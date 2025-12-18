import java.util.*;
import java.time.*;
import lombok.Data;

// Enums
enum UserStatus { ONLINE, OFFLINE, BUSY, AWAY }
enum MessageType { TEXT, FILE, IMAGE }
enum MeetingStatus { SCHEDULED, ONGOING, COMPLETED, CANCELLED }

// Models
@Data
class User {
    private String id;
    private String name;
    private String email;
    private UserStatus status;
    
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = UserStatus.OFFLINE;
    }
    
    @Override
    public String toString() { return name + " (" + status + ")"; }
}

@Data
class Message {
    private String id;
    private User sender;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    
    public Message(String id, User sender, String content, MessageType type) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", 
            timestamp.toLocalTime(), sender.getName(), content);
    }
}

@Data
class Channel {
    private String id;
    private String name;
    private User creator;
    private List<Message> messages = new ArrayList<>();
    private Set<User> members = new HashSet<>();
    
    public Channel(String id, String name, User creator) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.members.add(creator);
    }
    
    public void addMember(User user) { members.add(user); }
    public void removeMember(User user) { members.remove(user); }
    
    public void sendMessage(User sender, String content) {
        if (!members.contains(sender)) {
            throw new IllegalArgumentException("User not a member of channel");
        }
        Message msg = new Message(UUID.randomUUID().toString(), sender, content, MessageType.TEXT);
        messages.add(msg);
    }
    
    @Override
    public String toString() { 
        return "Channel: " + name + " (Creator: " + creator.getName() + ", Members: " + members.size() + ")"; 
    }
}

@Data
class Chat {
    private String id;
    private Set<User> participants;
    private List<Message> messages = new ArrayList<>();
    private boolean isGroup;
    
    public Chat(String id, Set<User> participants) {
        this.id = id;
        this.participants = participants;
        this.isGroup = participants.size() > 2;
    }
    
    public void sendMessage(User sender, String content) {
        if (!participants.contains(sender)) {
            throw new IllegalArgumentException("User not in chat");
        }
        Message msg = new Message(UUID.randomUUID().toString(), sender, content, MessageType.TEXT);
        messages.add(msg);
    }
    
    @Override
    public String toString() {
        String type = isGroup ? "Group Chat" : "Direct Chat";
        return String.format("%s (Participants: %d)", type, participants.size());
    }
}

@Data
class Meeting {
    private String id;
    private String title;
    private User organizer;
    private Set<User> participants = new HashSet<>();
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MeetingStatus status;
    
    public Meeting(String id, String title, User organizer, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.title = title;
        this.organizer = organizer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = MeetingStatus.SCHEDULED;
        this.participants.add(organizer);
    }
    
    public void addParticipant(User user) { participants.add(user); }
    public void removeParticipant(User user) { 
        if (!user.equals(organizer)) participants.remove(user);
    }
    
    public void start() { 
        if (status == MeetingStatus.SCHEDULED) {
            status = MeetingStatus.ONGOING;
            participants.forEach(u -> u.setStatus(UserStatus.BUSY));
        }
    }
    
    public void end() { 
        if (status == MeetingStatus.ONGOING) {
            status = MeetingStatus.COMPLETED;
            participants.forEach(u -> u.setStatus(UserStatus.ONLINE));
        }
    }
    
    public void cancel() { status = MeetingStatus.CANCELLED; }
    
    @Override
    public String toString() {
        return String.format("Meeting: %s (%s) [%s - %s] - %s", 
            title, organizer.getName(), startTime.toLocalTime(), 
            endTime.toLocalTime(), status);
    }
}

// Main System
@Data
class TeamsApplication {
    private Map<String, User> users = new HashMap<>();
    private Map<String, Channel> channels = new HashMap<>();
    private Map<String, Chat> chats = new HashMap<>();
    private Map<String, Meeting> meetings = new HashMap<>();
    
    // User Management
    public User createUser(String name, String email) {
        String userId = "U-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(userId, name, email);
        users.put(userId, user);
        return user;
    }
    
    public void setUserStatus(String userId, UserStatus status) {
        User user = users.get(userId);
        if (user != null) user.setStatus(status);
    }
    
    // Channel Management
    public Channel createChannel(String name, User creator) {
        String channelId = "CH-" + UUID.randomUUID().toString().substring(0, 8);
        Channel channel = new Channel(channelId, name, creator);
        channels.put(channelId, channel);
        return channel;
    }
    
    public void addMemberToChannel(String channelId, User user) {
        Channel channel = channels.get(channelId);
        if (channel != null) channel.addMember(user);
    }
    
    // Chat Management
    public Chat createDirectChat(User user1, User user2) {
        String chatId = "C-" + UUID.randomUUID().toString().substring(0, 8);
        Set<User> participants = new HashSet<>(Arrays.asList(user1, user2));
        Chat chat = new Chat(chatId, participants);
        chats.put(chatId, chat);
        return chat;
    }
    
    public Chat createGroupChat(Set<User> participants) {
        if (participants.size() < 3) {
            throw new IllegalArgumentException("Group chat needs at least 3 participants");
        }
        String chatId = "GC-" + UUID.randomUUID().toString().substring(0, 8);
        Chat chat = new Chat(chatId, participants);
        chats.put(chatId, chat);
        return chat;
    }
    
    // Meeting Management
    public Meeting scheduleMeeting(String title, User organizer, LocalDateTime start, LocalDateTime end) {
        String meetingId = "M-" + UUID.randomUUID().toString().substring(0, 8);
        Meeting meeting = new Meeting(meetingId, title, organizer, start, end);
        meetings.put(meetingId, meeting);
        return meeting;
    }
    
    public List<Meeting> getUserMeetings(User user) {
        List<Meeting> userMeetings = new ArrayList<>();
        for (Meeting meeting : meetings.values()) {
            if (meeting.getStatus() != MeetingStatus.CANCELLED && 
                meeting.getParticipants().contains(user)) {
                userMeetings.add(meeting);
            }
        }
        return userMeetings;
    }
}

// Demo
public class Main {
    public static void main(String[] args) {
        System.out.println("=== MICROSOFT TEAMS - SD2 INTERVIEW ===\n");
        
        TeamsApplication app = new TeamsApplication();
        
        // Create users
        User alice = app.createUser("Alice", "alice@company.com");
        User bob = app.createUser("Bob", "bob@company.com");
        User charlie = app.createUser("Charlie", "charlie@company.com");
        
        app.setUserStatus(alice.getId(), UserStatus.ONLINE);
        app.setUserStatus(bob.getId(), UserStatus.ONLINE);
        
        System.out.println("✓ Users created:");
        System.out.println("  " + alice);
        System.out.println("  " + bob);
        System.out.println("  " + charlie + "\n");
        
        // Create channels
        Channel general = app.createChannel("General", alice);
        Channel backend = app.createChannel("Backend", alice);
        
        app.addMemberToChannel(general.getId(), bob);
        app.addMemberToChannel(general.getId(), charlie);
        app.addMemberToChannel(backend.getId(), bob);
        
        System.out.println("✓ Channels created:");
        System.out.println("  " + general);
        System.out.println("  " + backend + "\n");
        
        // Send messages in channel
        general.sendMessage(alice, "Welcome everyone!");
        general.sendMessage(bob, "Thanks! Excited to be here.");
        general.sendMessage(charlie, "Hello team!");
        
        System.out.println("💬 Messages in #General:");
        general.getMessages().forEach(msg -> System.out.println("  " + msg));
        System.out.println();
        
        // Create direct chat
        Chat directChat = app.createDirectChat(alice, bob);
        directChat.sendMessage(alice, "Hey Bob, can we discuss the project?");
        directChat.sendMessage(bob, "Sure! Let's schedule a meeting.");
        
        System.out.println("💬 Direct Chat - " + directChat);
        directChat.getMessages().forEach(msg -> System.out.println("  " + msg));
        System.out.println();
        
        // Create group chat
        Set<User> groupUsers = new HashSet<>(Arrays.asList(alice, bob, charlie));
        Chat groupChat = app.createGroupChat(groupUsers);
        groupChat.sendMessage(alice, "Team sync at 3 PM?");
        groupChat.sendMessage(bob, "Works for me!");
        
        System.out.println("💬 " + groupChat);
        groupChat.getMessages().forEach(msg -> System.out.println("  " + msg));
        System.out.println();
        
        // Schedule meeting
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(1);
        Meeting meeting = app.scheduleMeeting("Sprint Planning", alice, start, end);
        meeting.addParticipant(bob);
        meeting.addParticipant(charlie);
        
        System.out.println("📅 Meeting scheduled: " + meeting);
        System.out.println("  Participants: " + meeting.getParticipants().size());
        System.out.println();
        
        // Start meeting
        meeting.start();
        System.out.println("📞 Meeting started!");
        System.out.println("  Alice status: " + alice.getStatus());
        System.out.println("  Bob status: " + bob.getStatus());
        System.out.println("  Charlie status: " + charlie.getStatus());
        System.out.println();
        
        // End meeting
        meeting.end();
        System.out.println("✓ Meeting ended!");
        System.out.println("  Alice status: " + alice.getStatus());
        System.out.println("  Meeting status: " + meeting.getStatus());
        System.out.println();
        
        // Get user meetings
        List<Meeting> aliceMeetings = app.getUserMeetings(alice);
        System.out.println("📋 Alice's meetings: " + aliceMeetings.size());
        
        System.out.println("\n=== DEMO COMPLETED ===");
    }
}