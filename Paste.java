import com.clipboarduploader.dto.UploadRequest;
import com.clipboarduploader.dto.UploadResponse;
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
    
    /*
        Uploads an image to the server's file system.
        Returns a set of ids beloning to the uploaded images.
    */
    @CrossOrigin(origins = "http://localhost:1841")
    @RequestMapping(value="/upload/imageUpload", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public UploadResponse uploadImage(@RequestBody UploadRequest uploadRequest)
    {
        System.out.println( uploadRequest );
        return new UploadResponse();
    }
    
    @RequestMapping(value="/getImage", method = RequestMethod.GET)
    public String getImage(@RequestParam("imageId") int imageId )
    {
        return imageId + "";
    }
}
-----------------------------------------------------
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
        image = ImageIO.read(new ByteArrayInputStream( imageBytes ) );
        return image;
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
	getBase64EncodedImage : function( image, imageEncodings, numOfImages, callbackFn )
	{
		if( image )
		{
			var remoteImage = new Image();
			remoteImage.crossOrigin = 'Anonymous';
			remoteImage.onerror = function(e)
			{
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
