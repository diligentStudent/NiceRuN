package com.nicerun.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public class SaveFile {

   public static enum FileType {
      Image, JSONFile
   }

   // type이 0이면 Image
   // type이 1이면 JSONFile
   public static List<String> SaveFIle(@RequestBody List<MultipartFile> pfile, @RequestParam FileType type) {
      int len = pfile.size();
      List<String> StringList = new ArrayList<>();
      FileType fileType = null;
      String path = new File("").getAbsolutePath();

      switch (type) {
      case Image:
         fileType = FileType.Image;
         // 이미지 저장할 폴더 설정
         path = path + "/image/";
         break;
      case JSONFile:
         fileType = FileType.JSONFile;
         // 파일 저장할 폴더 설정
         path = path + "/track/";
         break;
      }

      for (Iterator<MultipartFile> iter = pfile.iterator(); iter.hasNext();) {
         MultipartFile mf = iter.next();
//         UUID uuid = UUID.randomUUID();
         String saveName = mf.getOriginalFilename();
         File file = new File(path + saveName);

         try {
            mf.transferTo(file);
            StringList.add(file.getAbsolutePath());
         } catch (Exception e) {
            System.out.println("fail to copy File!!!");
            System.out.println("location: com.nicerun.util => SaveFile => SaveFile()");
            e.printStackTrace();
         }
      }
      return StringList;
   }
}