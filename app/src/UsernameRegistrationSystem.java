import java.util.*;

public class UsernameRegistrationSystem {

    public static void main(String[] args) {

        UsernameService service = new UsernameService();

        service.registerUser("alex");
        service.registerUser("john");
        service.registerUser("emma");
        service.registerUser("alex123");

        service.checkUsername("alex");
        service.checkUsername("johnny");
        service.checkUsername("alex");
        service.checkUsername("alex");

        service.displayPopularity();
    }
}

class UsernameService {

    private Set<String> registeredUsers;
    private Map<String, Integer> attemptedUsernames;

    public UsernameService() {
        registeredUsers = new HashSet<>();
        attemptedUsernames = new HashMap<>();
    }

    public void registerUser(String username) {
        registeredUsers.add(username);
    }

    public void checkUsername(String username) {

        attemptedUsernames.put(
                username,
                attemptedUsernames.getOrDefault(username, 0) + 1
        );

        if (registeredUsers.contains(username)) {
            System.out.println("Username '" + username + "' is already taken.");
            suggestUsernames(username);
        } else {
            System.out.println("Username '" + username + "' is available.");
        }
    }

    private void suggestUsernames(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!registeredUsers.contains(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        suggestions.add(username + "_official");
        suggestions.add(username + "_01");

        System.out.println("Suggestions: " + suggestions);
    }

    public void displayPopularity() {
        System.out.println("\nUsername Attempt Popularity:");

        for (Map.Entry<String, Integer> entry : attemptedUsernames.entrySet()) {
            System.out.println(entry.getKey() + " attempted " + entry.getValue() + " times");
        }
    }
}