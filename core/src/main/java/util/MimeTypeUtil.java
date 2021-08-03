package util;

import eu.medsea.mimeutil.MimeUtil;

import java.io.File;
import java.util.Collection;

/**
 * @author JwZheng
 * @date 2021/7/29 17:11
 */
public class MimeTypeUtil {
    public static String getType(File file) {
        if (file.getName().endsWith(".html")) {
            return Constant.DEFAULT_CONTENT_TYPE;
        }
        if (file.getName().endsWith(".ico")) {
            return "image/x-icon";
        }
        Collection mimeTypes = MimeUtil.getMimeTypes(file);
        return mimeTypes.toArray()[0].toString();
    }
}
