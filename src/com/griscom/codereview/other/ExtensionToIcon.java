package com.griscom.codereview.other;

import java.util.HashMap;
import java.util.Map;

import com.griscom.codereview.R;

public class ExtensionToIcon
{
    private static Map<String, Integer> map=new HashMap<String, Integer>();

    static
    {
        map.put("apk",   R.drawable.icon_apk);
		map.put("avi",   R.drawable.icon_avi);
		map.put("bat",   R.drawable.icon_bat);
		map.put("bz2",   R.drawable.icon_bz2);
		map.put("c",     R.drawable.icon_c);
		map.put("cmd",   R.drawable.icon_cmd);
		map.put("cpp",   R.drawable.icon_cpp);
		map.put("cs",    R.drawable.icon_cs);
		map.put("css",   R.drawable.icon_css);
		map.put("db",    R.drawable.icon_db);
		map.put("dll",   R.drawable.icon_dll);
		map.put("doc",   R.drawable.icon_doc);
		map.put("docx",  R.drawable.icon_docx);
		map.put("exe",   R.drawable.icon_exe);
		map.put("gif",   R.drawable.icon_gif);
		map.put("gz",    R.drawable.icon_gz);
		map.put("h",     R.drawable.icon_h);
		map.put("hpp",   R.drawable.icon_hpp);
		map.put("html",  R.drawable.icon_html);
		map.put("jar",   R.drawable.icon_jar);
		map.put("java",  R.drawable.icon_java);
		map.put("jpeg",  R.drawable.icon_jpeg);
		map.put("jpg",   R.drawable.icon_jpg);
		map.put("js",    R.drawable.icon_js);
		map.put("lua",   R.drawable.icon_lua);
		map.put("mp3",   R.drawable.icon_mp3);
		map.put("pas",   R.drawable.icon_pas);
		map.put("patch", R.drawable.icon_patch);
		map.put("png",   R.drawable.icon_png);
		map.put("ppt",   R.drawable.icon_ppt);
		map.put("pptx",  R.drawable.icon_pptx);
		map.put("pri",   R.drawable.icon_pri);
		map.put("pro",   R.drawable.icon_pro);
		map.put("py",    R.drawable.icon_py);
		map.put("rar",   R.drawable.icon_rar);
		map.put("sh",    R.drawable.icon_sh);
		map.put("sln",   R.drawable.icon_sln);
		map.put("so",    R.drawable.icon_so);
		map.put("tar",   R.drawable.icon_tar);
		map.put("txt",   R.drawable.icon_txt);
		map.put("ui",    R.drawable.icon_ui);
		map.put("unity", R.drawable.icon_unity);
		map.put("vbs",   R.drawable.icon_vbs);
		map.put("wav",   R.drawable.icon_wav);
		map.put("xls",   R.drawable.icon_xls);
		map.put("xlsx",  R.drawable.icon_xlsx);
		map.put("xml",   R.drawable.icon_xml);
		map.put("zip",   R.drawable.icon_zip);
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
