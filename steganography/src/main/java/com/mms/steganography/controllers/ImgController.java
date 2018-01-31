package com.mms.steganography.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mms.steganography.models.Image;

@Controller
public class ImgController {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(value = {"/home", "/"}, method = RequestMethod.GET)
    public String home(Model model) {
        model.addAttribute("image", new Image());
        return "home";
    }
	
	@RequestMapping(value = "/hide", method = RequestMethod.POST)
	public String hideMessage(Model model, @RequestParam("imageCode") MultipartFile image, 
			@RequestParam("messageCode") String message, @RequestParam(required = false, value = "checkCode") Boolean markInName,
			HttpServletRequest request, HttpServletResponse response) 
					throws IOException {
		if (message.equals("")) {
			model.addAttribute("error", "Please, write your message.");
			return "home";
		}
		//when we want to code data in image	
		//this method must download coded image at the end
		ByteArrayInputStream in = new ByteArrayInputStream(image.getBytes());
		BufferedImage bufferedImage = ImageIO.read(in);
		Image img = new Image(bufferedImage, message);
		
		boolean coded = img.encodeImage();
		if (coded) {
			//first write buffimage to some location, then download it from that location
			//we must convert format to png
			String originalName = image.getOriginalFilename().split("\\.")[0];
			if (markInName != null) {
				originalName += "Coded";
			}
			String fullPath = System.getProperty("user.dir") + "\\src\\main\\resources\\images\\" + originalName;
			String formatName = "png";
			ImageIO.write(img.getImg(), formatName, new File(fullPath));
			
			//now when image is saved, we must download it server
			//DOWNLOADING PART
			ServletContext context = request.getSession().getServletContext();
			
			File downloadFile = new File(fullPath);
			FileInputStream inputStream = new FileInputStream(downloadFile);
			
			String mimeType = context.getMimeType(fullPath);
		    if (mimeType == null) {
	           // set to binary type if MIME mapping not found
	           mimeType = "application/octet-stream";
		    }
		    response.setContentType(mimeType);
	        response.setContentLength((int) downloadFile.length());
	        response.setHeader("Content-Disposition", "attachment; filename=\"" + originalName + "." + formatName + "\"");
	        
	        //ServletOutputStream outStream = response.getOutputStream();
	        PrintWriter outStream = response.getWriter();
	        int bytesRead = -1;
	        //write bytes from the input to output stream
	        while ((bytesRead = inputStream.read()) != -1)
	            //outStream.write(buffer, 0, bytesRead);
	        	outStream.write(bytesRead);
	        
	        outStream.flush();
	        outStream.close();
	        inputStream.close();
	        
	        //delete image from the folder on the root
	        if (downloadFile.delete())
	        	log.info("Image deleted.");
	        else
	        	log.info("Image not deleted.");
		     
		}
		else
			model.addAttribute("error", "Message is too long for image of this size!");
		
		return "home";
	}
		
	@RequestMapping(value = "/show", method = RequestMethod.POST)
	public String showMessage(Model model, @RequestParam("imageDecode") MultipartFile image) throws IOException {
		//when we want to decode data from image
		ByteArrayInputStream in = new ByteArrayInputStream(image.getBytes());
		BufferedImage bufferedImage = ImageIO.read(in);
		Image img = new Image(bufferedImage, "");
		String decodedMessage = img.decodeImage();
		model.addAttribute("decodedMessage", decodedMessage);
		
		return "home";
	}
	
}
