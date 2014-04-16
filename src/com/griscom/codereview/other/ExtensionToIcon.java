package com.griscom.codereview.other;

import java.util.HashMap;
import java.util.Map;

import com.griscom.codereview.R;

public class ExtensionToIcon
{
    private static Map<String, Integer> map=new HashMap<String, Integer>();

    static
    {
        map.put("cs", R.drawable.icon_cs);
    }

    public static int getIcon(String extension)
    {
        Integer res=map.get(extension);

        if (res!=null)
        {
            return res.intValue();
        }

        return R.drawable.icon_file;
    }
}
