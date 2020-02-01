package pengguang.photoserver;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import javax.servlet.*;
import javax.servlet.http.*;

import com.drew.imaging.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.metadata.file.*;
import com.drew.metadata.jpeg.*;
import com.drew.imaging.jpeg.*;
import com.drew.lang.GeoLocation;

/**
 * Servlet implementation class LoginController
 */
public class Photo extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String album = request.getParameter("album");
		String photo = request.getParameter("photo");

		out.println("<HTML>");
		out.println("<HEAD><meta http-equiv=Content-Type content='text/html;charset=utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><TITLE>Album</TITLE>");
		out.println("<link rel='stylesheet' type='text/css' href='css/style.css'>");
		out.println("</HEAD>");
		out.println("<BODY>");
		out.println("<table width='100%'><tr><td><form action='album.html' style='display: inline;'><input type='hidden' name='album' value='"+album+"'/> <input type='submit' value='返回' /></form></td><td align='right'><form action='photo.html' style='display: inline;' method='post' onsubmit=\"return confirm('确认删除?');\"><nobr><input type='hidden' name='album' value='"+album+"'/><input type='hidden' name='photo' value='"+photo+"'/><input type='submit' value='删除'/></form></td></tr></table>");

		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				if (file.isHidden())
					return false;
				if (file.isDirectory())
					return false;

				String name = file.getName();
				if (name.substring(name.lastIndexOf('.')+1).toLowerCase().matches("jpeg|jpg|mp4")) return true;
				return false;
			}
		};
		File path = new File(Util.ROOT_PATH+"/"+album);
		File[] files = path.listFiles(filter);
		if (files != null) {
			out.println("<table>");

			Arrays.sort(files);
			int i = 0;
			for (i=0; i<files.length; i++) {
				if (files[i].getName().equals(photo)) {
					String prevForm = "";
					String nextForm = "";
					if (i-1 >= 0) {
						prevForm = String.format("<form action='photo.html' style='display: inline;'><input type='hidden' name='album' value='%s'/><input type='hidden' name='photo' value='%s'/><input type='submit' value='&#x25c4;' /></form>", album, files[i-1].getName());
					}
					if (i+1 < files.length) {
						nextForm = String.format("<form action='photo.html' style='display: inline;'><input type='hidden' name='album' value='%s'/><input type='hidden' name='photo' value='%s'/><input type='submit' value='&#x25ba;' /></form>", album, files[i+1].getName());
					}
					out.println(String.format("<tr><td>%s</td><td align='right'>%s</td></tr>", prevForm, nextForm));
					if (photo.substring(photo.lastIndexOf('.')+1).toLowerCase().matches("jpeg|jpg")) {
					    String img_path = String.format("%s/%s/%s", Util.ROOT_PATH, album, photo);
						out.println(String.format("<tr><td colspan='2'><a href='%s'><img src='%s' style='max-width: 100%%'/></a></td></tr>", img_path, img_path));
					} else {
						out.println(String.format("<tr><td colspan='2'><video width='640' controls><source src='%s/%s/%s' type='video/mp4'></video></td></tr>", Util.ROOT_PATH, album, photo));
					}
					break;
				}
			}

			if (i < files.length) {
				try {
					Metadata metadata = ImageMetadataReader.readMetadata(files[i]);
					FileSystemDirectory file = metadata.getFirstDirectoryOfType(FileSystemDirectory.class);
					if (file != null) {
						out.println(String.format("<tr><td colspan='2'>%s: %s</td></tr>", "文件", file.getString(FileSystemDirectory.TAG_FILE_NAME)));
						out.println(String.format("<tr><td colspan='2'>%s: %s</td></tr>", "大小", file.getString(FileSystemDirectory.TAG_FILE_SIZE)));
					}
					JpegDirectory dir = metadata.getFirstDirectoryOfType(JpegDirectory.class);
					if (dir != null) {
						out.println(String.format("<tr><td colspan='2'>%s: %sx%s</td></tr>", "尺寸", dir.getString(JpegDirectory.TAG_IMAGE_WIDTH), dir.getString(JpegDirectory.TAG_IMAGE_HEIGHT)));
					}
					ExifIFD0Directory exif = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
					if (exif != null) {
					    String model = exif.getString(ExifIFD0Directory.TAG_MODEL); 
					    String make = exif.getString(ExifIFD0Directory.TAG_MAKE);
                        if (model != null) {
                            if (make != null) {
                                out.println(String.format("<tr><td colspan='2'>%s: %s(%s)</td></tr>", "设备", model, make));
                            } else {
                                out.println(String.format("<tr><td colspan='2'>%s: %s</td></tr>", "设备", model));
                            }
                        }

						String datetime = exif.getString(ExifIFD0Directory.TAG_DATETIME);
                        if (datetime != null) {
                            out.println(String.format("<tr><td colspan='2'>%s: %s</td></tr>", "时间", datetime));
                        }
					}
					GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    if (gps != null) {
                        GeoLocation loc = gps.getGeoLocation();
                        if (loc != null) {
                            String longitude = GeoLocation.decimalToDegreesMinutesSecondsString(loc.getLongitude());
                            String latitude = GeoLocation.decimalToDegreesMinutesSecondsString(loc.getLatitude());
                            if (longitude != null && latitude != null) {
                                out.println(String.format("<tr><td colspan='2'>%s: 经度%s, 纬度%s</td></tr>", "位置", longitude, latitude));
                            }
                        }
                    }
				} catch (Exception e) {
					Log.d(""+e);
				}
			}
			out.println("</table>");
		}
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String album = request.getParameter("album");
		String photo = request.getParameter("photo");

		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");

		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				if (file.isHidden())
					return false;
				if (file.isDirectory())
					return false;

				String name = file.getName();
				if (name.substring(name.lastIndexOf('.')+1).toLowerCase().matches("jpeg|jpg|mp4")) return true;
				return false;
			}
		};
		File path = new File(Util.ROOT_PATH+"/"+album);
		File[] files = path.listFiles(filter);
		String redirect = "album.html?album="+URLEncoder.encode(album, "UTF-8");
		if (files != null) {
			Arrays.sort(files);
			int i;
			for (i=0; i<files.length; i++) {
				if (files[i].getName().equals(photo)) {
					if (i+1 < files.length) {
						redirect = "photo.html?album="+URLEncoder.encode(album, "UTF-8")+"&photo="+URLEncoder.encode(files[i+1].getName(), "UTF-8");
					} else if (i-1 >= 0) {
						redirect = "photo.html?album="+URLEncoder.encode(album, "UTF-8")+"&photo="+URLEncoder.encode(files[i-1].getName(), "UTF-8");
					}
					break;
				}
			}
		}

		/* delete file */
		File file = new File(String.format("%s/%s/%s", Util.ROOT_PATH, album, photo));
		if (file.exists()) {
			file.delete();
		}

		/* delete thumbnail */
		file = new File(String.format("%s/%s/.thumbnail/%s", Util.ROOT_PATH, album, photo));
		if (file.exists()) {
			file.delete();
		}

		response.sendRedirect(redirect);
	}
}

