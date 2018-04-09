package com.xmu.lxq.aiad.config;


/**
 * Created by HoHo on 2018/1/22 0022.
 */

public class Config {

    //public static final String sdcard_url= Environment.getExternalStorageDirectory().getPath();
    private static final String sdcard_url= "/sdcard";
    //项目'AIAD'的url
    private static final String project_url=sdcard_url+"/AIAD";
    //userfiles
    public static final String userfiles_url=project_url+"/userfiles";
    //RGBafter
    private static final String RGBafter_url=userfiles_url+"/RGBafter";
    //dealfiles
    private static final String dealfiles_url=project_url+"/dealfiles";
    //deal
    public static final String deal_url=dealfiles_url+"/deal";
    //resourcesfiles
    public static final String resourcesfiles_url=project_url+"/resourcesfiles/videos";

    public  static final String[]  directorysUrl={project_url,userfiles_url,RGBafter_url,dealfiles_url,deal_url,resourcesfiles_url};
}
