package com.griscom.codereview.other;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.griscom.codereview.R;

public class ExtensionToIcon
{
    private static final String TAG="ExtensionToIcon";

    private static Map<String, Integer> map=new HashMap<String, Integer>();

    static
    {
        Field[] drawables=R.drawable.class.getFields();

        for (int i=0; i<drawables.length; ++i)
        {
            try
            {
                String drawableName=drawables[i].getName();

                if (drawableName.startsWith("_icon_"))
                {
                    String extension=drawableName.substring(6);

                    if (
                        !extension.equals("file")
                        &&
                        !extension.equals("folder")
                       )
                    {
                        map.put(extension, drawables[i].getInt(null));
                    }
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "Impossible to get drawables", e);
            }
        }
    }

    public static int getIcon(String extension)
    {
        Integer res=map.get(extension);

        if (res!=null)
        {
            return res.intValue();
        }

        return R.drawable.__icon_file;
    }
}
