package com.example.credit.Views;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.credit.Activitys.MainActivity;
import com.example.credit.Entitys.DataManager;
import com.example.credit.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Decoder.BASE64Decoder;

public class FileUtil {

    /**
     * 将Bitmap 图片保存到本地路径，并返回路径
     *
     * @param c
     * @param mType    资源类型，参照  MultimediaContentType 枚举，根据此类型，保存时可自动归类
     * @param fileName 文件名称
     * @param bitmap   图片
     * @return
     */
    public static String saveFile(Context c, String fileName, Bitmap bitmap) {
        return saveFile(c, "", fileName, bitmap);
    }

    public static String saveFile(Context c, String filePath, String fileName, Bitmap bitmap) {
        byte[] bytes = bitmapToBytes(bitmap);
        return saveFile(c, filePath, fileName, bytes);
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String saveFile(Context c, String filePath, String fileName, byte[] bytes) {
        String fileFullName = "";
        FileOutputStream fos = null;
        try {
            String suffix = "";
            if (filePath == null || filePath.trim().length() == 0) {
                filePath = Environment.getExternalStorageDirectory() + "/Credit/cache" + "/";
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fullFile = new File(filePath, fileName + suffix);
            fileFullName = fullFile.getPath();
            fos = new FileOutputStream(new File(filePath, fileName + suffix));
            fos.write(bytes);
        } catch (Exception e) {
            fileFullName = "";
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    fileFullName = "";
                }
            }
        }
        return fileFullName;
    }

//	public static String SDPATH = Environment.getExternalStorageDirectory()+ "/formats/";//获取文件夹

    /**
     * 删除文件
     *
     * @param fileName
     */
    public static void delFile(String fileName) {//传文件路径
        File file = new File(fileName);
        if (file.isFile()) {
            file.delete();
        }
        file.exists();
    }

    /**
     * 删除文件夹和文件夹里面的文件
     *
     * @param path
     */
    public static void deleteDir(String path) {//传文件夹路径
        File dir = new File(path);
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;

        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDir(path); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 保存图片
     *
     * @param bm
     * @param picName
     */
    public static void saveBitmap(Bitmap bm, String picName) {
        try {
            if (!isFileExist("")) {
                File tempf = createSDDir("");
            }
            File f = new File(picName);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createSDDir(String dirName) throws IOException {
        File dir = new File(dirName);
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            System.out.println("createSDDir:" + dir.getAbsolutePath());
            System.out.println("createSDDir:" + dir.mkdir());
        }
        return dir;
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        file.isFile();
        return file.exists();
    }

    /**
     * 解决图片内存溢出问题方法
     *
     * @param path
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static Bitmap decodeBitmap(String path, int screenWidth, int screenHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true; //设置为true, 加载器不会返回图片, 而是设置Options对象中以out开头的字段.即仅仅解码边缘区域
        BitmapFactory.decodeFile(path, opts);
        // 得到图片的宽和高
        int imageWidth = opts.outWidth;
        int imageHeight = opts.outHeight;
        // 计算缩放比例
        int widthScale = imageWidth / screenWidth;
        int heightScale = imageHeight / screenHeight;
        int scale = widthScale > heightScale ? widthScale : heightScale;
        // 指定加载可以加载出图片.
        opts.inJustDecodeBounds = false;
        // 使用计算出来的比例进行缩放
        opts.inSampleSize = scale;
        Bitmap bmp = BitmapFactory.decodeFile(path, opts);
            return bmp;
    }


    /**
     * 判断Android程序是否在前台运行的方法
     */
    public static boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 版本名
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    /**
     * 版本号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

    /**
     * //创建文件夹
     */
    public static void filenewsexists() {
        File destDir1 = new File(Environment.getExternalStorageDirectory() + "/Credit");//项目文件夹
        File destDir2 = new File(Environment.getExternalStorageDirectory() + "/Credit/cache");//项目缓存文件夹
        File destDir3 = new File(Environment.getExternalStorageDirectory() + "/Credit/TwoDimImg");//项目存放二维码文件夹
        if (!destDir1.exists()) {
            destDir1.mkdirs();
        }
        if (!destDir2.exists()) {
            destDir2.mkdirs();
        }
        if (!destDir3.exists()) {
            destDir3.mkdirs();
        }
    }
    public static void imgscache() {
        if (DataManager.LBimgS.data.carouselInfo != null && DataManager.LBimgS.data.carouselInfo.size()>0) {
            for (int i = 0; i < DataManager.LBimgS.data.carouselInfo.size(); i++) {
                if (DataManager.LBimgS.data.carouselInfo.get(i).PATH != null) {
                    try {
                        BASE64Decoder decode = new BASE64Decoder();
                        byte[] b = decode.decodeBuffer(DataManager.LBimgS.data.carouselInfo.get(i).PATH);
                        //把字节数组的图片写到另一个地方
                        File apple = new File(Environment.getExternalStorageDirectory() + "/Credit/cache/CarouselImg"+i+".jpg");
                        FileOutputStream fos = new FileOutputStream(apple);
                        fos.write(b);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 判断email格式是否正确
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    /**
     * 判断是否全是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}
