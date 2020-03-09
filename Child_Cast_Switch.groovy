/*
 *  Google Home Assistant Relay v3
 *
 *  Copyright 2020 Ryan P. Casler
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  
 *    Date        Who            What
 *    ----        ---            ----
 *    2020-03-09  Ryan Casler    Initial Release
 *
 */
metadata {
    definition(name: "Child Cast Switch", namespace: "ryan780", author: "Ryan780", importURL: "https://raw.githubusercontent.com/ryancasler/Hubitat_Assistant_Relay/master/Child_Cast_Switch.groovy") {
        capability "Actuator"
        capability "Switch"
        command "toggle"
        command "castWebsite", [[name:"Website*", type: "STRING", description: "Enter the website to cast."]]
        command "castMedia", [[name:"Media Path or URL*", type: "STRING", description: "Enter the path or URL for the media to be cast."]]
    }
    preferences{
        input "defaultType", "enum", title: "Cast Type", required: true, options:[["website":"Website"],["local":"Local Media"],["remote":"Remote Media"]]
        input "defaultContent", "string", title: "Website URL or media path", required: true
        input "castDevice", "string", title: "Device name", required: true
        input "maxOn", "number", title: "Max On time in seconds", required: false, description:"Enter the Max On time, if desired"
        input name: "enableMute", type: "bool", title: "Enable mute/unmute", defaultValue: true
        input name: "enableStop", type: "bool", title: "Enable Stop after timeout", defaultValue: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def installed(){
    updated()
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable){
        runIn(1800, logsOff)
    }else{
        unschedule(logsOff)
    }
    if(settings.maxOn){
        state.maxOn = settings.maxOn.toInteger()
    }else{
        state.maxOn = false
    }
}


def parse(String description) {
    if (logEnable) log.debug(description)
}

def on() {
    if (logEnable) log.debug "Switching ON.  Casting ${defaultType} ${defaultContent}"
    if(device.currentValue("switch") !="on"){
        if(enableMute){
            parent.mute(castDevice)
        }
        parent.castContent(castDevice, defaultType, defaultContent)
    }
    if(state.maxOn){
        unschedule(off)
        runIn (state.maxOn, off)
    }
    sendEvent(name:"switch", value:"on")
}

def off() {
    unschedule(off)
    if (logEnable) log.debug "OFF called"
    if(enableStop){
        parent.stop(castDevice)
    }
    if (enableMute){
        parent.unmute(castDevice)
    }
    sendEvent(name: "switch", value:"off")
}

def toggle(){
    if(device.currentValue("switch")=="on"){
        off()
    }else{
        on()
    }
}


def castWebsite(website){
    if (logEnable) log.debug "Casting website ${website}"
    if(device.currentValue("switch") !="on"){
        if (enableMute){
            parent.mute(castDevice)
        }
        parent.castContent(castDevice, "website", website)
        if(state.maxOn){
            runIn (state.maxOn, off)
        }
        sendEvent(name:"switch", value:"on")
    }
}

def castMedia(media){
    if (logEnable) log.debug "Casting meida ${media}"
    if(device.currentValue("switch") !="on"){
        if (enableMute){
            parent.mute(castDevice)
        }
        parent.castContent(castDevice, "remote", media)
        if(state.maxOn){
            runIn (state.maxOn, off)
        }
        sendEvent(name:"switch", value:"on")
    }
}