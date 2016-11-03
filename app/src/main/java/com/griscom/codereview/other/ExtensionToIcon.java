package com.griscom.codereview.other;

import com.griscom.codereview.R;
import com.griscom.codereview.util.AppLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension to icon conversion
 */
@SuppressWarnings("WeakerAccess")
public final class ExtensionToIcon
{
    @SuppressWarnings("unused")
    private static final String TAG = "ExtensionToIcon";



    private static Map<String, Integer> sMap = new HashMap<>(100);



    static
    {
        Field[] drawables = R.drawable.class.getFields();

        for (Field drawable : drawables)
        {
            try
            {
                String drawableName = drawable.getName();

                if (drawableName.startsWith("_icon_"))
                {
                    String extension = drawableName.substring(6);

                    //noinspection NonFinalStaticVariableUsedInClassInitialization
                    sMap.put(extension, drawable.getInt(null));
                }
            }
            catch (Exception e)
            {
                AppLog.wtf(TAG, "Impossible to get drawables", e);
            }
        }
    }

    /**
     * Disabled default constructor
     */
    private ExtensionToIcon()
    {
        // Nothing
    }

    /**
     * Gets icon resource ID for specified extension
     * @param extension    extension
     * @return icon resource ID
     */
    public static int getIcon(String extension)
    {
        Integer res = sMap.get(extension);

        if (res != null)
        {
            return res;
        }

        return R.drawable.__icon_file;
    }
}
