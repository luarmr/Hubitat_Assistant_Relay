# Hubitat_Assistant_Relay
Repository for drivers to interface to the Google Assistant Relay from Hubitat Elevation Hub

Installation: Follow the instructions listed here to install custom drivers in Hubitat.  Be sure to install the parent driver first, then the child driver. 

https://docs.hubitat.com/index.php?title=Drivers_Code

Known Limitations:
All of these are following are limitations that cannot be worked around or fixed by anyone except Google as they are there by Google's design, mostly for security.  
1. Your NodeJS sever and your Google devices must be on the same subnet of your network and not blocked by a VLAN.  Failure to do so will cause GAR to report success but no broadcast to be issued from your speaker
2. Your router cannot use IP v6.  Doing so will cause the Google Assistant Service to think that your speakers are on a different network than your GAR server and cause the service to report success but no broadcast to be played on your speakers
3. You cannot control streaming content from other sources from GAR.  For example: you cannot issue the command: “Stream music from Spotify on Kitchen speaker”.  This will result in the Assistant Service reporting an error.  However, via the stop command through CATT, you can stop this media from playing.
4. You cannot run Google Routines from GAR.  This is blocked by the Assistant Service.
5. GAR cannot be run in the cloud.  It must be on the same network as your Google Home speakers/displays in order to issue commands or broadcasts.
6. Once you stream other content to you speaker/display, you cannot resume the previous content that was playing.  For example, you are listening to Sirius XM on your Google Nest Home Hub.  A rule fires and your dashboard is streamed to your display.  Once that stops, there is no way for GAR to resume playing Sirius XM on your display.  You would have to request it to stream that content again manually.  
