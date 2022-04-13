package farm.nurture.laminar.core.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;

public class CoreUtil {

    private CoreUtil(){}
    @SuppressWarnings("UnstableApiUsage")
    public static String readAsString(String resource) {
        try {
            URL url = Resources.getResource(resource);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

}
