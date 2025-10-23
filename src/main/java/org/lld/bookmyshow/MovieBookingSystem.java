import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


public class MovieBookingSystem {


    // --- Models ---
    static class User {
        String username, password;
        User(String u, String p) { this.username = u; this.password = p; }
    }


    static class Seat {
        int seatNo;
        Seat(int no) { this.seatNo = no; }
    }


    static class Theatre {
        String name;
        List<Screen> screens = new ArrayList<>();
        Theatre(String name) { this.name = name; }
    }


    static class Screen {
        String screenName;
        List<Seat> seats = new ArrayList<>();
        List<Show> shows = new ArrayList<>();
        Screen(String name, int totalSeats) {
            this.screenName = name;
            for (int i = 1; i <= totalSeats; i++) seats.add(new Seat(i));
        }
    }


    static class Show {
        String movie;
        Date start, end;
        Screen screen;
        int price;
        Map<Integer, ShowSeatReservation> reservations = new ConcurrentHashMap<>();


        Show(String movie, Date start, Date end, Screen s, int price) {
            this.movie = movie;
            this.start = start;
            this.end = end;
            this.screen = s;
            this.price = price;
            for (Seat seat : s.seats) reservations.put(seat.seatNo, new ShowSeatReservation(seat));
        }
    }


    static class ShowSeatReservation {
        Seat seat;
        boolean booked;
        final Lock lock = new ReentrantLock();
        ShowSeatReservation(Seat s) { this.seat = s; }
    }


    // --- Payment Service ---
    interface PaymentService {
        boolean pay(String user, int amount);
        boolean refund(String user, int amount);
    }
    static class DummyPaymentService implements PaymentService {
        public boolean pay(String user, int amount) {
            System.out.println("Payment of Rs." + amount + " by " + user + " successful.");
            return true;
        }
        public boolean refund(String user, int amount) {
            System.out.println("Refund of Rs." + amount + " to " + user + " successful.");
            return true;
        }
    }


    // --- Core System ---
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<Theatre> theatres = new CopyOnWriteArrayList<>();
    private final PaymentService paymentService = new DummyPaymentService();


    public boolean register(String u, String p) {
        return users.putIfAbsent(u, new User(u, p)) == null;
    }


    public Theatre addTheatre(String name) {
        Theatre t = new Theatre(name);
        theatres.add(t);
        return t;
    }


    public Screen addScreen(Theatre t, String screenName, int seats) {
        Screen s = new Screen(screenName, seats);
        t.screens.add(s);
        return s;
    }


    public boolean addShow(String movie, Date start, Date end, Screen s, int price) {
        // Prevent overlap within the same screen
        for (Show existing : s.shows) {
            if (start.before(existing.end) && end.after(existing.start)) {
                System.out.println("Conflict: " + s.screenName + " already has a show at this time.");
                return false;
            }
        }
        Show show = new Show(movie, start, end, s, price);
        s.shows.add(show);
        return true;
    }


    public boolean bookSeat(Show show, int seatNo, String user) {
        ShowSeatReservation res = show.reservations.get(seatNo);
        res.lock.lock();
        try {
            if (!res.booked) {
                if (paymentService.pay(user, show.price)) {
                    res.booked = true;
                    System.out.println("User " + user + " booked Seat " + seatNo
                            + " for " + show.movie + " in " + show.screen.screenName);
                    return true;
                }
                return false;
            } else {
                System.out.println("Seat " + seatNo + " already booked!");
                return false;
            }
        } finally { res.lock.unlock(); }
    }


    public boolean cancelSeat(Show show, int seatNo, String user) {
        ShowSeatReservation res = show.reservations.get(seatNo);
        res.lock.lock();
        try {
            if (res.booked) {
                res.booked = false;
                paymentService.refund(user, show.price);
                System.out.println("User " + user + " cancelled Seat " + seatNo
                        + " for " + show.movie);
                return true;
            }
            return false;
        } finally { res.lock.unlock(); }
    }


    public List<Show> getAllShows() {
        List<Show> allShows = new ArrayList<>();
        for (Theatre t : theatres) {
            for (Screen s : t.screens) {
                allShows.addAll(s.shows);
            }
        }
        return allShows;
    }


    // --- Demo ---
    public static void main(String[] args) {
        MovieBookingSystem sys = new MovieBookingSystem();
        sys.register("alice", "123");
        sys.register("bob", "456");


        Theatre t = sys.addTheatre("PVR");
        Screen s1 = sys.addScreen(t, "Screen1", 5);
        Screen s2 = sys.addScreen(t, "Screen2", 5);


        sys.addShow("Inception", new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 7200000), s1, 200);
        sys.addShow("Interstellar", new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + 7200000), s2, 250);


        Show show1 = s1.shows.get(0);
        Show show2 = s2.shows.get(0);


        sys.bookSeat(show1, 1, "alice");
        sys.bookSeat(show2, 1, "bob");
        sys.bookSeat(show2, 1, "nishant");
    }
}

