package com.griscom.codereview.other;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.util.AppLog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Extension to icon conversion
 */
@SuppressWarnings({"WeakerAccess", "SpellCheckingInspection"})
public final class ExtensionToIcon
{
    @SuppressWarnings("unused")
    private static final String TAG = "ExtensionToIcon";



    private static final int INITIAL_CAPACITY = 107;



    @SuppressWarnings("ConstantNamingConvention")
    private static final Map<String, Integer> sMap = new HashMap<>(INITIAL_CAPACITY);



    static
    {
        sMap.put("apk",     R.drawable._icon_apk);
        sMap.put("asm",     R.drawable._icon_asm_s_vsz);
        sMap.put("s",       R.drawable._icon_asm_s_vsz);
        sMap.put("vsz",     R.drawable._icon_asm_s_vsz);
        sMap.put("avi",     R.drawable._icon_avi);
        sMap.put("bat",     R.drawable._icon_bat_cmd);
        sMap.put("cmd",     R.drawable._icon_bat_cmd);
        sMap.put("bmp",     R.drawable._icon_bmp);
        sMap.put("bsc",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("def",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("i",       R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("idl",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("inc",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("inl",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("lic",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("lst",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("map",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("mdp",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("odh",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("odl",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("tli",     R.drawable._icon_bsc_def_i_idl_inc_inl_lic_lst_map_mdp_odh_odl_tli);
        sMap.put("bz2",     R.drawable._icon_bz2_gz_tar_xz);
        sMap.put("gz",      R.drawable._icon_bz2_gz_tar_xz);
        sMap.put("tar",     R.drawable._icon_bz2_gz_tar_xz);
        sMap.put("xz",      R.drawable._icon_bz2_gz_tar_xz);
        sMap.put("c",       R.drawable._icon_c_cod);
        sMap.put("cod",     R.drawable._icon_c_cod);
        sMap.put("cc",      R.drawable._icon_cc_cpp_cxx);
        sMap.put("cpp",     R.drawable._icon_cc_cpp_cxx);
        sMap.put("cxx",     R.drawable._icon_cc_cpp_cxx);
        sMap.put("cs",      R.drawable._icon_cs);
        sMap.put("css",     R.drawable._icon_css);
        sMap.put("db",      R.drawable._icon_db_dll_so);
        sMap.put("dll",     R.drawable._icon_db_dll_so);
        sMap.put("so",      R.drawable._icon_db_dll_so);
        sMap.put("dmp",     R.drawable._icon_dmp_mdmp);
        sMap.put("mdmp",    R.drawable._icon_dmp_mdmp);
        sMap.put("doc",     R.drawable._icon_doc);
        sMap.put("docx",    R.drawable._icon_docx);
        sMap.put("dsp",     R.drawable._icon_dsp_vcp_vcproj);
        sMap.put("vcp",     R.drawable._icon_dsp_vcp_vcproj);
        sMap.put("vcproj",  R.drawable._icon_dsp_vcp_vcproj);
        sMap.put("dsw",     R.drawable._icon_dsw);
        sMap.put("egg",     R.drawable._icon_egg);
        sMap.put("exe",     R.drawable._icon_exe);
        sMap.put("exp",     R.drawable._icon_exp);
        sMap.put("filters", R.drawable._icon_filters);
        sMap.put("flac",    R.drawable._icon_flac);
        sMap.put("gif",     R.drawable._icon_gif);
        sMap.put("h",       R.drawable._icon_h_hpp_hxx_tlh);
        sMap.put("hpp",     R.drawable._icon_h_hpp_hxx_tlh);
        sMap.put("hxx",     R.drawable._icon_h_hpp_hxx_tlh);
        sMap.put("tlh",     R.drawable._icon_h_hpp_hxx_tlh);
        sMap.put("html",    R.drawable._icon_html);
        sMap.put("ico",     R.drawable._icon_ico);
        sMap.put("idb",     R.drawable._icon_idb);
        sMap.put("ilk",     R.drawable._icon_ilk);
        sMap.put("jar",     R.drawable._icon_jar);
        sMap.put("java",    R.drawable._icon_java);
        sMap.put("jpeg",    R.drawable._icon_jpeg_jpg);
        sMap.put("jpg",     R.drawable._icon_jpeg_jpg);
        sMap.put("js",      R.drawable._icon_js);
        sMap.put("lib",     R.drawable._icon_lib);
        sMap.put("lua",     R.drawable._icon_lua);
        sMap.put("m4a",     R.drawable._icon_m4a);
        sMap.put("mak",     R.drawable._icon_mak_mk);
        sMap.put("mk",      R.drawable._icon_mak_mk);
        sMap.put("mp3",     R.drawable._icon_mp3);
        sMap.put("ncb",     R.drawable._icon_ncb);
        sMap.put("obj",     R.drawable._icon_obj);
        sMap.put("pas",     R.drawable._icon_pas);
        sMap.put("patch",   R.drawable._icon_patch);
        sMap.put("pch",     R.drawable._icon_pch);
        sMap.put("pdb",     R.drawable._icon_pdb);
        sMap.put("pdf",     R.drawable._icon_pdf);
        sMap.put("png",     R.drawable._icon_png);
        sMap.put("ppt",     R.drawable._icon_ppt);
        sMap.put("pptx",    R.drawable._icon_pptx);
        sMap.put("pri",     R.drawable._icon_pri);
        sMap.put("pro",     R.drawable._icon_pro);
        sMap.put("props",   R.drawable._icon_props_vsprops);
        sMap.put("vsprops", R.drawable._icon_props_vsprops);
        sMap.put("psd",     R.drawable._icon_psd);
        sMap.put("py",      R.drawable._icon_py);
        sMap.put("pyc",     R.drawable._icon_pyc);
        sMap.put("pycon",   R.drawable._icon_pycon);
        sMap.put("qml",     R.drawable._icon_qml);
        sMap.put("rar",     R.drawable._icon_rar);
        sMap.put("rgs",     R.drawable._icon_rgs);
        sMap.put("sbr",     R.drawable._icon_sbr_srf);
        sMap.put("srf",     R.drawable._icon_sbr_srf);
        sMap.put("sh",      R.drawable._icon_sh);
        sMap.put("sln",     R.drawable._icon_sln);
        sMap.put("swf",     R.drawable._icon_swf);
        sMap.put("tiff",    R.drawable._icon_tiff);
        sMap.put("txt",     R.drawable._icon_txt);
        sMap.put("ui",      R.drawable._icon_ui);
        sMap.put("unity",   R.drawable._icon_unity);
        sMap.put("vb",      R.drawable._icon_vb);
        sMap.put("vbs",     R.drawable._icon_vbs);
        sMap.put("vcxproj", R.drawable._icon_vcxproj);
        sMap.put("wav",     R.drawable._icon_wav);
        sMap.put("wma",     R.drawable._icon_wma);
        sMap.put("xls",     R.drawable._icon_xls);
        sMap.put("xlsx",    R.drawable._icon_xlsx);
        sMap.put("xml",     R.drawable._icon_xml);
        sMap.put("zip",     R.drawable._icon_zip);



        if (BuildConfig.DEBUG)
        {
            Field[] drawables = R.drawable.class.getFields();
            int extensionCount = 0;

            for (Field drawable : drawables)
            {
                try
                {
                    String drawableName = drawable.getName();

                    if (drawableName.startsWith("_icon_"))
                    {
                        String[] extensions = drawableName.substring(6).split("_");
                        int drawableId = drawable.getInt(null);

                        for (String extension : extensions)
                        {
                            AppLog.d(TAG, "extension = " + extension + " drawable = " + drawableName);

                            ++extensionCount;
                            Integer mappedId = sMap.get(extension);

                            if (mappedId != null)
                            {
                                if (mappedId != drawableId)
                                {
                                    AppLog.wtf(TAG, "Incorrect drawable set for extension \"" + extension + '\"');
                                }
                            }
                            else
                            {
                                AppLog.wtf(TAG, "Drawable is not set for extension \"" + extension + '\"');
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    AppLog.wtf(TAG, "Impossible to get drawables", e);
                }
            }

            if (extensionCount != INITIAL_CAPACITY)
            {
                AppLog.wtf(TAG, "Incorrect INITIAL_CAPACITY value. It should be equal to " + extensionCount);
            }

            if (extensionCount != sMap.size())
            {
                AppLog.wtf(TAG, "Some extensions are missing");
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
