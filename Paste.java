/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.controller;

import com.clipboarduploader.dto.UploadRequest;
import com.clipboarduploader.dto.UploadResponse;
import com.clipboarduploader.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Anton
 */
@RestController 
public class UploadController {
    
    @Autowired
    UploadService uploadService;
    /*
        Uploads an image to the server's file system.
        Returns a set of ids beloning to the uploaded images.
    */
    @CrossOrigin(origins = "http://localhost:1841")
    @RequestMapping(value="/upload/imageUpload", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public UploadResponse uploadImage(@RequestBody UploadRequest uploadRequest)
    {
        return uploadService.saveImages(uploadRequest);
    }
    
    @RequestMapping(value="/getImage", method = RequestMethod.GET)
    public String getImage(@RequestParam("imageId") int imageId )
    {
        return imageId + "";
    }
}
-----------------------------------------------------
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.dao;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Anton
 */
public interface IImageDAO {
    
    public List<Long> saveImages( List<BufferedImage> images );
}
------------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.dao;

import com.clipboarduploader.constant.FilePaths;
import com.clipboarduploader.util.TimeUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/**
 *
 * @author Anton
 */
@Component
public class ImageDAOImpl implements IImageDAO {

    public ImageDAOImpl()
    {}
    
    @Override
    public List<Long> saveImages(List<BufferedImage> images) {
        List<Long> imageIds = new ArrayList<Long>();
        long currentId;
        File imageFile;
        for(BufferedImage image : images )
        {
            try
            {
                currentId = TimeUtil.currentTimeAsLong();
                imageIds.add( currentId );
                String filePath = FilePaths.IMAGE_SAVE_DIRECTORY + "//" + String.valueOf( currentId ) + ".jpeg";
                System.out.println( filePath );
                imageFile = new File( FilePaths.IMAGE_SAVE_DIRECTORY, String.valueOf( currentId ) + ".jpeg");
                if( !imageFile.exists())
                    imageFile.createNewFile();
                ImageIO.write(image, "jpeg", imageFile);
            }
            catch(IOException e)
            {
                System.out.println( e.getMessage() );
            }
        }
        return imageIds;
    }
    
}
--------------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.dto;

import org.springframework.stereotype.Component;

/**
 *
 * @author Anton
 */
@Component
public class UploadRequest {
    
    private String encodingType;
    private String[] encodings;
    
    public UploadRequest()
    {}
    
    public UploadRequest(String encodingType, String[] encodings)
    {
        this.encodingType = encodingType;
        this.encodings = encodings;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public String[] getEncodings() {
        return encodings;
    }

    public void setEncodings(String[] encodings) {
        this.encodings = encodings;
    }
    
    public String toString()
    {
        StringBuilder strBldr = new StringBuilder();
        strBldr.append( this.getEncodingType() );
        strBldr.append("\n");
        for(String encoding : encodings )
        {
            strBldr.append( encoding );
            strBldr.append("\n");
        }
        return strBldr.toString();
    }
}
----------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.dto;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @author Anton
 */
@Component
public class UploadResponse {
    
    private boolean success;
    private int uploadedImageCount;
    private List<Long> imageIds;

    public UploadResponse()
    {}
    
    public UploadResponse(boolean success, int uploadedImageCount, List<Long> imageIds) {
        this.success = success;
        this.uploadedImageCount = uploadedImageCount;
        this.imageIds = imageIds;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getUploadedImageCount() {
        return uploadedImageCount;
    }

    public void setUploadedImageCount(int uploadedImageCount) {
        this.uploadedImageCount = uploadedImageCount;
    }

    public List<Long> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<Long> imageIds) {
        this.imageIds = imageIds;
    }
}
------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.service;

import com.clipboarduploader.dao.ImageDAOImpl;
import com.clipboarduploader.dto.UploadRequest;
import com.clipboarduploader.dto.UploadResponse;
import com.clipboarduploader.util.ImageUtil;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 *
 * @author Anton
 */
@Component
public class UploadService {
    
    @Autowired
    ImageDAOImpl imageDao;
    
    public UploadService()
    {}
    
    public UploadResponse saveImages(UploadRequest request)
    {
        UploadResponse response = new UploadResponse();
        //Get the BufferedImages
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for(String encoding : request.getEncodings() )
        {
            try
            {
                images.add( ImageUtil.base64EncodingToBufferedImage(encoding) );
            }
            catch(IOException e)
            {
                e.printStackTrace();
                System.out.println( "Cannot convert encoding: " + encoding );
            }
        }
        List<Long> imageIds = imageDao.saveImages(images);
        //Pass the images to the DAO which will save them + return a set of ids
        //if successful, pass the ids to response object
        response.setImageIds(imageIds);
        response.setUploadedImageCount(imageIds.size());
        response.setSuccess(imageIds.size() != 0);
        return response;
    }
    
}
----------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Anton
 */
public class ImageUtil {
    
    /*
        Converts a String base64 encoded image into a BufferedImage object that can be stored
        as a file.
    */
    public static BufferedImage base64EncodingToBufferedImage( String encodedImage ) throws IOException
    {
        BufferedImage image = null;
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(encodedImage);
        image = ImageIO.read(new ByteArrayInputStream( imageBytes ));
       
        BufferedImage rgbAdjustedImage = new BufferedImage( image.getWidth(),image.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] rgb = image.getRGB(0, 0, image.getWidth(),image.getHeight(), null, 0, image.getWidth());
        rgbAdjustedImage.setRGB(0, 0, image.getWidth(),image.getHeight(), rgb, 0, image.getWidth());
        
        return rgbAdjustedImage;
    }
    
}
----------------------------------------------------
	/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clipboarduploader.util;

import java.sql.Timestamp;


/**
 *
 * @author Anton
 */
public class TimeUtil {
    
    public static long currentTimeAsLong()
    {
        Timestamp currentTime = new Timestamp( System.currentTimeMillis() );
        String currentTimeStr = currentTime.toString().replaceAll(":|\\s|-|\\.", "");
        return Long.parseLong(currentTimeStr);
    }
}


----------------------------------------------------------
Ext.define('NoteKeeper.view.tabs.PasteUploader',{
	extend : 'Ext.container.Container',
	alias : 'widget.pasteUploader',
	width : 400,
	height : 100,
	initComponent : function()
	{
		var me = this;
		Ext.define('Override.form.field.VTypes', {
		    override: 'Ext.form.field.VTypes',

		    // vtype validation function
		    htmlImage: function(value) {
		        return this.htmlImageRe.test(value);
		    },
		    // RegExp for the value to be tested against within the validation function
		    htmlImageRe: /<img[^>]+src="([^">]+)".*>/g,
		    // vtype Text property: The error text to display when the validation function returns false
		    htmlImageText: 'Not a valid time.  Must be in the format "12:34 PM".'
		});

		//<img[^>]+src="([^">]+)".*>
		var htmlEditor = Ext.widget('htmleditor',{
			itemId : 'htmlEditor',
			scrollable : false,
			readOnly : false,
			listeners : {
				afterrender : function( editor )
				{
					editor.getToolbar().hide();
				},
				change : function( editor, newValue, oldValue, eOpts )
				{
					//console.log( newValue );
				},
				initialize : function( editor )
				{
					editor.getEditorBody().onpaste = function( event )
					{
						//console.log( event );
					}
				},
				beforesync : function( editor, html, eOpts )
				{
					//console.log( editor );
					//console.log( html );
					//console.log( eOpts );
					// /return false;
				}
			}
		});
		var btn = Ext.widget('button',{
			handler : function() {
				//var matches = ( htmlEditor.getValue().toString().match(/<img[^>]+src="([^">]+)"/g) || [] );
				//console.log( htmlEditor.getValue().toString() );
				//console.log( htmlEditor.getValue().toString().match(/<img[^>]+src="([^">]+)"/g).length );
				var imageElements = htmlEditor.getEditorBody().getElementsByTagName('img');
				console.log( imageElements );
				if( imageElements.length === 0 )
					Ext.Msg.alert("Invalid Input", "Please paste only images!");
				else
				{
					me.extractAndSendImages( imageElements );
					//var base64Encodings = me.getBase64ImageEncoding( imageElements );
					//console.log( base64Encodings );
				}
			}
		});

		this.items = [
			htmlEditor,
			btn
		];
		this.callParent( arguments );
	},
	extractAndSendImages : function( imageElementArr )
	{
		this.imageEncodings = [];
		var imageTag = undefined;
		for( var k = 0; k < imageElementArr.length; k++)
		{ 
			imageTag = imageElementArr[k];
			imageSrc = imageTag.src;
			if( imageSrc && imageSrc.indexOf('base64') === -1 )
			{
				this.getBase64EncodedImage( imageTag, this.imageEncodings, imageElementArr.length, this.sendImages );
			}
			else
			{
				this.imageEncodings.push( imageSrc.split(',')[1] );
			}
		}

		if( this.imageEncodings.length === imageElementArr.length )
			this.sendImages( this.imageEncodings, 'http://localhost:8080/upload/imageUpload');
	},
	getBase64ImageEncoding : function( imageElementArr )
	{
		// var imageEncodings = [];
		// var imageTag = undefined;
		// for( var k = 0; k < imageElementArr.length; k++)
		// { 
		// 	imageTag = imageElementArr[k];
		// 	imageSrc = imageTag.src;
		// 	if( imageSrc && imageSrc.indexOf('base64') === -1 )
		// 	{
		// 		imageEncodings.push( this.getEncodedImage(imageTag) );
		// 	}
		// 	else
		// 	{
		// 		imageEncodings.push( imageSrc.split(',')[1] );
		// 	}
		// }
		// return imageEncodings;
	},
	getBase64EncodedImage : function( image, imageEncodings, numOfImages, callbackFn )
	{
		if( image )
		{
			var remoteImage = new Image();
			remoteImage.crossOrigin = 'Anonymous';
			remoteImage.onerror = function(e)
			{
				console.log('failed to fetch image')
				numOfImages--;
				if( imageEncodings.length === numOfImages )
					callbackFn( imageEncodings, 'http://localhost:8080/upload/imageUpload' );
			}
			remoteImage.onload = function()
			{
				var canvas = document.createElement( 'canvas' );
				var context = canvas.getContext('2d');
				canvas.height = this.naturalHeight;
    			canvas.width = this.naturalWidth;
				context.drawImage( this, 10, 10 );
				var dataURL = canvas.toDataURL();
				var encoding = dataURL.split(',')[1];
				imageEncodings.push( encoding );
				if( imageEncodings.length === numOfImages )
					callbackFn( imageEncodings, 'http://localhost:8080/upload/imageUpload' );
			}
			remoteImage.src = image.src;
			if (remoteImage.complete || remoteImage.complete === undefined) {
			    remoteImage.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==";
			    remoteImage.src = image.src;
			}
		}
	},
	sendImages : function( imageEncodings, url)
	{
		if( imageEncodings.length > 0 )
		{
			var dataObj = {};
			dataObj[ "encodingType" ] = 'base64';
			dataObj[ "encodings" ] = imageEncodings;
			console.log( dataObj );

			Ext.Ajax.request({
				method : 'POST',
				url : url,
				jsonData : dataObj,
				headers : {
					'Content-Type' : 'application/json'
				},
				success : function() {
					alert('succeeded')
				},
				failure : function() {
					alert('failed')
				}
			});
		}
	}
});
