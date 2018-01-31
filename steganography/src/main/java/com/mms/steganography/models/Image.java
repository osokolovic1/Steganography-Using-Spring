package com.mms.steganography.models;

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class Image {
	
	private BufferedImage img;
	private String message;
	
	private int maxX, maxY;
	private int usedX, usedY;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public Image(BufferedImage img, String message) {
		super();
		this.img = img;
		this.message = message;
		
		this.usedX = this.usedY = 0;
		this.maxX = this.img.getHeight();
		this.maxY = this.img.getWidth(); 
	}
	
	public Image() {
		this.usedX = this.usedY = 0;
	}
	
	public BufferedImage getImg() {
		return img;
	}
	
	public void setImg(BufferedImage img) {
		this.img = img;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}
	
	/*	when using getRGB
	 * bitpos    10987654 32109876 54321098 76543210
		------   +--------+--------+--------+--------+
		bits     |AAAAAAAA|RRRRRRRR|GGGGGGGG|BBBBBBBB|   
		
		We will put one bit of message in every G and B part*
		and it will be from left to right(Green is first)	*/
	
	/*For every byte of message we need 4 bytes of image.
	  First 16 pixels(code has 32 bits so we need 16 pixels(GB) 
	  used for store the information that image is coded   */
	
	public boolean encodeImage() {
		//message with terminal simbol
		String codedMessage = "code" + this.message + "~";
		//if image is to small form message
		byte[] messageBytes = codedMessage.getBytes();
		if (maxX * maxY - 40 < messageBytes.length * 4)
			return false;
		//prolaziti kroz bajtove u poruci i smještati po bit u svaki rgb iz slike
		for (int i = 0; i < messageBytes.length; i++)
			encodeByte(messageBytes[i]);
		
		return true;
		
	}
	
	private void encodeByte(byte b) {
		int count = 0;	
		int x = this.usedX, y = this.usedY;
		
		for (this.usedY = y; count < 4 && this.usedY < this.maxY;) {
			for (this.usedX = x; count < 4; this.usedX++, count++) {
				//code pixel
				//reading from right to left, and writing in the same way - firstInPair on B and second on G
				boolean firstInPair = getBit(b, count * 2);
				boolean secondInPair = getBit(b, count * 2 + 1);
				// a |= (1 << bitindex) - sets bit of a with given index on 1
				// a &= ~(1 << bitindex) - sets bit of a with given index on 0
				int rgb = this.img.getRGB(this.usedY, this.usedX);
				if (firstInPair) 
					rgb |= (1 << 0);
				else
					rgb &= ~(1 << 0);
					
				if (secondInPair)
					rgb |= (1 << 8);
				else
					rgb &= ~(1 << 8);
				this.img.setRGB(this.usedY, this.usedX, rgb);

				//if we are at the end of the row but we must continue in the next row
				if (this.usedX == this.maxX - 1) {
					this.usedY++;
					this.usedX = -1; //-1 because end of this loop will increase it on 0
				}
			}
		}	
	}
	
	public String decodeImage() {
		String message = "";
		int pixelNum = 0, bitCount = 0;
		byte[] character = new byte[1]; //must be array because of conversion to string
		
		for (int y = 0; y < this.maxY; y++) {
			for (int x = 0; x < this.maxX; x++, bitCount++) {
				pixelNum++;
				//decode (y, x) pixel
				int rgb = this.img.getRGB(y, x);
				byte firstByte = (byte)(rgb & 0x000000FF);
				byte secondByte = (byte)((rgb & 0x0000FF00) >> 8);
				boolean firstBit = getBit(firstByte, 0);
				boolean secondBit = getBit(secondByte, 0);
				
				if (firstBit)
					character[0] |= (1 << bitCount * 2);
				else
					character[0] &= ~(1 << bitCount * 2);
				
				if (secondBit)
					character[0] |= (1 << bitCount * 2 + 1);
				else
					character[0] &= ~(1 << bitCount * 2 + 1);
				
				if (pixelNum % 4 == 0) {
					//we must add letter to string and restart letter
					String l = new String(character);
					//if character is terminal simbol return message
					if (l.equals("~")) {
						this.message = message.substring(4); //remove "code" from beggining
						return this.message; 
					}
					
					message += l;
					character[0] &= 0x00;
					bitCount = -1; //-1 because of increase at the end of loop
				}
				if (pixelNum == 16 && !message.equals("code"))
					//check if "code" is at the beggining, if not, image is not coded
					return "This image is does not contain any messages.";
			}
		}
		
		return message;
	}
	
	private boolean getBit(byte b, int position) {
		return (1 == ((b >> position) & 1));
	}

}
