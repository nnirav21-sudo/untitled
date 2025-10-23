package nishant.lld.moviebooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

enum SeatType{REGULAR(100),PREMIUM(200),VIP(300);
    private final int price;
    SeatType(int price){
        this.price=price;
    }
    public int getPrice(){
        return price;
    }
}
enum SeatStatus{
    BOOKED,AVAILABLE;
}
enum BookingStatus{
    CONFIRMED,CANCELLED;
}
class Seat{
    private String id;
    private SeatType type;
    private SeatStatus status;
    public Seat(int row,int col,SeatType type){
        this.id = row+"_"+col;
        this.status = SeatStatus.AVAILABLE;
        this.type = type;
    }
    public String getId(){
        return id;
    }
    public boolean isAvailable(){
        return status == SeatStatus.AVAILABLE;
    }
    public void book(){
        status = SeatStatus.BOOKED;
    }
    public void relaese(){
        status = SeatStatus.AVAILABLE;
    }
    public SeatType getType(){
        return type;
    }

    @Override
    public String toString(){
        return String.format("Seat(%s,%s,%s)",id,type,status);
    }
}
class Screen{
    private String id;
    private String name;
    private List<List<Seat>> seats;
    public Screen(String id,String name,int row,int col){
        this.id = id;
        this.name = name;
        initializeSeats(row,col);
    }

    private void initializeSeats(int row,int col){
        this.seats = new ArrayList<>();
        for(int i=0;i<row;i++){
            List<Seat> seatRow = new ArrayList<>();
            for(int j=0;j<col;j++){
                SeatType seatType= i>=row-2?SeatType.PREMIUM:i>=row-5?SeatType.VIP:SeatType.REGULAR;
                seatRow.add(new Seat(i,j,seatType));
            }
            seats.add(seatRow);
        }
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public Seat getSeat(int row,int col){
        if(row<seats.size() && col<seats.get(0).size()){
            return seats.get(row).get(col);
        }
        return null;
    }
    public List<Seat> getAvailableSeats(){
        List<Seat> availableSeats = new ArrayList<>();
        for(int i=0;i<seats.size();i++){
            for(int j=0;j<seats.get(0).size();j++){
                if(seats.get(i).get(j).isAvailable()){
                    availableSeats.add(seats.get(i).get(j));
                }
            }

        }
        return availableSeats;
    }

}
class Movie{
    private String id;
    private String name;
    private int duration;
    public Movie(String id,String name, int duration){
        this.id = id;
        this.name = name;
        this.duration = duration;
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public int getDuration(){
        return duration;
    }
    @Override
    public String toString(){
        return "Movie(" + name + "duration :" + duration + ")";
    }
}

class Show{
    private String id;
    private Screen screen;
    private Movie movie;
    private String startTime; // can be taken as Date also
    private final Object lock = new Object();

    public Show(String id, Screen screen, Movie movie, String startTime){
        this.id = id;
        this.screen = screen;
        this.movie=movie;
        this.startTime = startTime;
    }
    public String getId(){
        return id;
    }
    public Screen getScreen(){
        return screen;
    }
    public Movie getMovie(){
        return movie;
    }

    public List<Seat> getAvaiableSeats(){
        return screen.getAvailableSeats();
    }
    public  boolean bookSeats(List<String> seatIds){
        synchronized(lock){
            List<Seat> seatsToBook = new ArrayList<>();
            for(int i=0;i<seatIds.size();i++){
                String[] parts = seatIds.get(i).split("-");
                Seat seat = screen.getSeat(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
                if(!seat.isAvailable()){
                    return false;
                }
                seatsToBook.add(seat);
            }
            seatsToBook.forEach(Seat :: book);
        }
        return true;
    }

    public void releaseSeats(List<String> seatIds){
        synchronized(lock){
            for(int i=0;i<seatIds.size();i++){
                String[] parts = seatIds.get(i).split("-");
                Seat seat = screen.getSeat(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
                if(seat!=null) seat.relaese();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Show(%s, Screen: %s, Time: %s)",
                movie.getName(), screen.getName(), startTime);
    }

}
class Booking{
    private static int counter = 1;
    private String id;
    private String userId;
    private Show show;
    private List<Seat> seats;
    private BookingStatus bookingStatus;
    private int totalAmount;
    public Booking(String userId,Show show,List<Seat> seats){
        this.userId = userId;
        this.id=String.valueOf(counter++);
        this.show = show;
        this.seats = seats;
        this.bookingStatus = BookingStatus.CONFIRMED;
        this.totalAmount = seats.stream().mapToInt(s->s.getType().getPrice()).sum();
    }
    public String getId(){
        return id;
    }
    public BookingStatus getBookingStatus(){
        return bookingStatus;
    }
    public boolean cancelBooking(){
        if(bookingStatus==BookingStatus.CONFIRMED){
        bookingStatus = BookingStatus.CANCELLED;
        show.releaseSeats(seats.stream().map(s->s.getId()).collect(Collectors.toList()));
        return true;
        }
        return false;
    }
}
class MovieBookingSystem{
    private Map<String,Movie> allMovies = new HashMap<>();
    private Map<String,Show> allShows = new HashMap<>();
    private Map<String,Booking> allBookings = new HashMap<>();
    private Map<String,List<Booking>> bookingByUSer = new HashMap<>();

    public void addMovie(Movie movie){
        allMovies.put(movie.getId(),movie);
    }
    public void addShow(Show show){
        allShows.put(show.getId(),show);
    }
    public Booking createBooking(String userId,String showId,List<String> seatIds){
        Show show = allShows.get(showId);
        if(show==null || show.bookSeats(seatIds)==false){
            return null;
        }
        List<Seat> seats = new ArrayList<>();
        for (String seatId : seatIds) {
            String[] parts = seatId.split("-");
            Seat seat = show.getScreen().getSeat(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            if (seat != null) seats.add(seat);
        }
        Booking booking = new Booking(userId,show,seats);
        allBookings.put(booking.getId(),booking);
        bookingByUSer.computeIfAbsent(userId,k->new ArrayList<>()).add(booking);
        return booking;

    }
    public boolean cancelBooking(String bookingId){
       Booking booking = allBookings.get(bookingId);
       return booking != null && booking.cancelBooking();
    }
    public List<Booking> getUserBookings(String userId){
        return bookingByUSer.getOrDefault(userId,new ArrayList<>());
    }
}
public class MovieBookingDemo {
    public static void main(String[] args) {
        MovieBookingSystem bookingSystem = new MovieBookingSystem();
        Screen screen1 = new Screen("sc_1","Screen1",10,10);
        Screen screen2 = new Screen("sc_2","Screen2",8,10);
        Movie movie1 = new Movie("mov1","DDLJ",184);
        Movie movie2 = new Movie("mov2","RAJ",147);
        Show show1 = new Show("show1",screen1,movie1,"05-12-2025");
        Show show2 = new Show("show2",screen2,movie2,"05-12-2025");
        bookingSystem.addMovie(movie1);
        bookingSystem.addMovie(movie2);
        bookingSystem.addShow(show1);
        bookingSystem.addShow(show2);
        System.out.println("available seats:"+show1.getAvaiableSeats().size());
        Booking b1 = bookingSystem.createBooking("U1","show1",List.of("1-2","8-8","9-9","7-7"));
        System.out.println("Booking created: " + b1);
        Booking b2 = bookingSystem.createBooking("U2","show1",List.of("1-4","8-7","9-9","7-7"));
        System.out.println("Duplicate booking: " + (b2 == null ? "Failed (as expected)" : "Succeeded"));

    }
}
