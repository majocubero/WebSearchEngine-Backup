package ri.wse.support.validation;

public class ValidationUtils {

    private static final String idRegex = "^^[0-9]{9,10}$$";

    /**
     * Checks is the format of the ID is correct.
     * @param id the ID of the user
     * @return true if the ID is valid or false otherwise
     * */
    public static boolean isIdCorrect(String id){
        return id.matches(idRegex);
    }
}
