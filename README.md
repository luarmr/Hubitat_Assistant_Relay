# Hubitat_Assistant_Relay
Repository for drivers to interface to the Google Assistant Relay from Hubitat Elevation Hub

Installation: Follow the instructions listed here to install custom drivers in Hubitat.  Be sure to install the parent driver first, then the child driver. 

https://docs.hubitat.com/index.php?title=Drivers_Code

Background:
First off, there has been no change to the existing features of the AR driver.  The custom commands still work with [CC] and custom commands with converse true still use [CCC].  You can still issue broadcasts the same as you did in V1 and V2.  Those are all the same.

The new features are all related to Casting Websites and Media to your Google speakers and displays.  That can be accomplished in 3 different ways, each of them becoming simpler from a daily use perspective but more complicated from a setup perspective.

First option:  Directly from the parent device:
There is a custom command in the parent GAR device called Cast Content.  There are 3 parameters you have to send along with this command.  They are: device name to cast content to, type of content to cast and source of that content.

The device name must either be the IP address of your Google device or the proper device name, properly cased to match whatever name you have assigned to the device in the Google Home app.  The content type must be one of 3 options, website (for a website), remote(for remote media or non-standard local media) or local (for locale media on a Windows machine).  The last option, local, I will not be going into since this does not work when you run GAR on a Linux based system like a Raspberry Pi.  However, the remote option does work with local media.  You just have to do one special thing in the source parameter.  

Which brings us to source.  Source is either the full URL of the website you want to cast to the display. Or the URL or filepath of the media you want to cast.  The URL must be a direct download (playable) link (no Dropbox or Google Drive links to splash pages will work).  You can also use the URL for any local dashboard or the URL of any accessible HTTP camera stream (RTSP will not work).  You can also use the full file path for any local media on your nodeJS server.  Let me say that again as to be clear...the media must be on your NODEJS SERVER in order to use it via the cast feature.  You cannot use an FTP URL or Samba share or any other means to share media to the server.  Only an HTPP url or a local (to the server) filepath will work.  

This limitation is imposed by CATT, not by GAR and not by me.  If you want to be able use other means of sharing files, you are welcome to request that the folks over at CATT add them.  ANY request to expand this list of available means of casting will be ignored as I or the developer of GAR have any means of expanding it.  Use what is available or build your own or request it from them, those are your only options.  

The second and simpler way to cast media to your speaker, speaker group or display is with a child device.  In the parent device there is a command to create a child device after specifying the name of that device.  Once created, you can go into the child device to specify a few things.  

In the child device there is an option for the cast device name as well as default type and default content.  If the child device is turned on, the default content will be cast to you display/speaker.  You can create as many child devices as you like.  I have one for each “thing” I typically cast.  So, one each for 3 dashboards and 2 each for 2 cameras.  That way, I just have to turn on the appropriate device to get that content.

But if you don't want that many devices, you can use the commands Cast Website and Cast Media inside the child device to cast to the display you specify in the settings of the child device.  The string required either as the default option or using the 2 custom commands are the same as what is required in the parent device.  Either a fully encoded URL (including http://) or a full filepath of the location of the media on your server.  

Also available in the child device are 3 additional features which make it the more desirable way to control your content.

There is an option called Max On.  This option lets your specify in seconds, the maximuim time the content should be displayed or played.  The switch attribute on the device will automatically turn off at the end of this timeout.  You can also turn the switch off manually.

When that switch is turned off, via the option Enable Stop, you can have a stop command sent to your display, removing the website or the cast icon for media.  This is useful for speakers if you are using your Google Speakers as alarm devices with an alarm sound.  Stopping will stop the media from playing.  However, you should note, there is a good 2-4 second delay from the time the stop is requested by Hubitat to the time the display/speaker stops playing.  This is the time it takes GAR and Catt and Google Assistant to process the request.

The final option available is Enable Mute.  This allows you to mute your Google device before casting to it.  This option is not so useful for any media that involves sound but great for content that doesn't.  Annoyed by the “Cast Tone” that happens before you cast a dashboard to your Google Home Hub?  Well, enable mute eliminates that sound from playing before casting the media to your display.  If enabled, your display will be unmuted when the switch is turned off, either automatically or manually.

Because of the options of Enable Mute and Enable Stop, you may want to do what I have done for 2 dashboards I have set up to only be used on my Google Home Hub.  On those dashboards, I have added the child cast device so I can turn it off manually.  That way I can unmute the display and stop displaying the dashboard early when I am done with it.  You can also swipe right on your Google display to remove the dashboard, however, to manually unmute the speaker you would have to swipe up from the bottom and select the volume icon or ask to unmute via a voice command.  Having the cast  device on the dashboard is a quick way for your to accomplish the same thing.  If you don't want to have the cast device itself, you can have a hidden virtual button that turns off the cast device.  This is what I've done for my “Notifications” dashboard.

It should also be noted, that there is no keyboard on your Google Nest Hub.  So, you cannot have any type of pin associated with your dashboard.  They must be able to be displayed automatically.  Also, if any of your camera streams require basic authentication, you can enter that into the URL for the stream.  For example, if your camera is displayed by going to the URL http://192.168.1.200:8081 but you have to enter a usermane and passwords, you can encode them into the url like this:
http://username:password@192.168.1.200:8081
This works for all sites that use a basic authentication model.