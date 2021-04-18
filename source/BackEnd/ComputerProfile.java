package BackEnd;

/**
 * A dummy profile class that emulates a human player's profile.
 *
 * @author Samuel Fuller
 */
public class ComputerProfile extends Profile {

    /**
     * Creates a new computer profile.
     *
     * @param suffix the string to append to the end of the computer player's name
     */
    public ComputerProfile(String suffix) {
        super("Computer " + suffix, "computer", 0, 0, 0);
    }
}
