package com.griscom.codereview.other;

import java.util.HashMap;
import java.util.Map;

import com.griscom.codereview.R;

public class ExtensionToIcon
{
    private static Map<String, Integer> map=new HashMap<String, Integer>();

    static
    {
        map.put("bat",  R.drawable.icon_bat);
		map.put("c",    R.drawable.icon_c);
		map.put("cpp",  R.drawable.icon_cpp);
		map.put("cs",   R.drawable.icon_cs);
		map.put("h",    R.drawable.icon_h);
		map.put("hpp",  R.drawable.icon_hpp);
		map.put("java", R.drawable.icon_java);
		map.put("sh",   R.drawable.icon_sh);
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
