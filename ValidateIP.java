
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class ValidateIP{
 
    private static Pattern pattern;
    private static Matcher matcher;
 
    private static final String IP_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 
   /**
    * validate and return true or false
    */
    public static boolean validate(final String ip){	
    pattern = Pattern.compile(IP_PATTERN);
    matcher = pattern.matcher(ip);
    return matcher.matches();	    	    
    }
}