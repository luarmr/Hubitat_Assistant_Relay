/*
 *  Google Home Assistant Relay v3
 *
 *  Copyright 2020 Ryan Casler
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2020-03-12  Ryan Casler    Initial Release
 *
 *
 *  Based on the Google Assistnt Relay driver from Daniel Ogorchock 
 *
 *  Orignal Software License:
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *    Credit goes to Greg Hesp's work on the SmartThings platform as a starting point for this version!
 */

metadata {
    definition (name: "Google Home Assistant Relay v3", namespace: "ryan780", author: "Ryan780", importUrl: "https://raw.githubusercontent.com/ryancasler/Hubitat_Assistant_Relay/master/Google_Assistant_Relay_v3.groovy") {
        capability "Speech Synthesis"
        capability "Notification"
        command "newChildDevice", [[name: "New Child Device Name*", type: "STRING", description: "Enter the name of the new child device"]]
        command "mute", [[name:"Cast Device", type: "STRING", description: "If nothing is entered, the default device will be used."]]
        command "unmute", [[name:"Cast Device", type: "STRING", description: "If nothing is entered, the default device will be used."]]
        command "castContent", [[name:"Cast Device",type: "STRING", description: "If nothing is entered, the default device will be used."],
                                [name: "Cast Type*",type: "ENUM", constraints:["website","local","remote"], description: "Content type"],
                                [name:"Source*", type: "STRING", description: "Cast sourse"]]
        command "stop", [[name:"Cast Device", type: "STRING", description: "If nothing is entered, the default device will be used."]]
    }

    preferences {
        input(name: "serverIP", type: "string", title:"Server IP Address", description: "Enter IP Address of your Assistant Relay Server", required: true, displayDuringSetup: true)
        input(name: "serverPort", type: "string", title:"Server Port", description: "Enter Port of your Assistant Relay Server (defaults to 3000)", defaultValue: "3000", required: true, displayDuringSetup: true)
        input(name: "user", type: "string", title:"Assistant Relay Username", description: "Enter the username for this device", defaultValue: "", required: false, displayDuringSetup: true)
        input(name: "defaultDevice", type: "string", title:"Display Device IP or Name", description: "Enter the IP address or name of your dispplay device", required: false)
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def installed() {
    updated()
}

def updated() {
    if (logEnable){
        runIn(1800,logsOff)
    }else{
        unschedule(logsOff)
    }
}

def initialialized(){
    updated()
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def parse(String description) {
    //if (logEnable) log.debug "Parsing ${description}"
}

def speak(message) {
    message = message.replaceAll("%20", " ")
    message = message.replace("Å", "\\u00C5")
    message = message.replace("å", "\\u00E5")
    message = message.replace("Ä", "\\u00C4")
    message = message.replace("ä", "\\u00E4")
    message = message.replace("Ö", "\\u00D6")
    message = message.replace("ö", "\\u00F6")
    message = message.replace("Ø", "\\u00D8")
    message = message.replace("Æ", "\\u00C6")
    message = message.replace("æ", "\\u00E6")
    message = message.replace("ø", "\\u00F8")
    def myJSON = ""
    if(message.startsWith("[CC]")){ 
        message = message.minus("[CC]")
        if (user) {
            myJSON = "{ \"command\": \"${message}\",\"user\": \"${user}\" }"
        } else {
            myJSON = "{ \"command\": \"${message}\" }"
        }
    }  
    else if(message.startsWith("[CCC]")){ 
        message = message.minus("[CCC]")
        if (user) {
            myJSON = "{ \"command\": \"${message}\",\"user\": \"${user}\",\"converse\": \"true\" }"
        } else {
            myJSON = "{ \"command\": \"${message}\",\"converse\": \"true\" }"
        }
    } 
    else if(message.startsWith("[P]")){ 
        message = message.minus("[P]")
        if (user) {
            myJSON = "{ \"preset\": \"${message}\",\"user\": \"${user}\" }"
        } else {
            myJSON = "{ \"preset\": \"${message}\" }"
        }
    } 
    else {
        if (user) {
            myJSON = "{ \"command\": \"${message}\",\"user\": \"${user}\",\"broadcast\": \"true\" }"
        } else {
            myJSON = "{ \"command\": \"${message}\",\"broadcast\": \"true\" }"
        }
    }
    httpPostJSON(myJSON)  
}

def deviceNotification(message) {
    speak(message)
}

def httpPostJSON(myJSON) {
    try {
        if (logEnable) log.debug "Sending ${myJSON} to ${serverIP}:${serverPort}"
        def headers = [:]
        headers.put("HOST", "${serverIP}:${serverPort}")
        headers.put("Content-Type", "application/json")
        def method = "POST"
        def path = "/assistant"
        def result = new hubitat.device.HubAction(
            method: method,
            path: path,
            body: myJSON,
            headers: headers
        )
        return result
    } catch (Exception e) {
        log.error "Error = ${e}"
    } 
}

def newChildDevice(deviceName){
    def newId = device.id + "-"+deviceName
    log.info "Creating Child Device ${deviceName} with ID: ${newId})'"
    try {            
        addChildDevice("Child Cast Switch", "${newId}",[label: "${deviceName}", isComponent: false, name: "${device.displayName}-${deviceName}"])         
    } catch (e) {
        log.error "Child device creation failed with error = ${e}"
    }
}

def mute(castDevice){
    if(logEnable)log.debug "Muting ${castDevice}."
    def message = "Mute ${castDevice}"
    def myJSON = "{ \"command\": \"${message}\",\"user\": \"${user}\" }"
    httpPostJSON(myJSON)
}

def mute(){
    if(defaultDevice){
        if(logEnable)log.debug "Muting default device."
        mute(defaultDevice)
    }else{
        log.warn "No default device speficied. Sepcify device in command parameter or driver properties."
    }
}

def unmute(castDevice){
    if(logEnable)log.debug "Unmuting ${castDevice}."
    def message = "Unmute ${castDevice}"
    def myJSON = "{ \"command\": \"${message}\",\"user\": \"${user}\" }"
    httpPostJSON(myJSON)
}

def unmute(){
    if(defaultDevice){
        if(logEnable)log.debug "Unmuting default device."
        unmute(defaultDevice)
    }else{
        log.warn "No default device speficied. Sepcify device in command parameter or driver properties."
    }
}

def castContent(castDevice, type, source){
    if(logEnable)log.debug "Casting ${source} of ${type} to ${castDevice}" 
    def myJSON = "{ \"type\": \"${type}\",\"source\": \"${source}\",\"device\": \"${castDevice}\" }"
    try {
        if (logEnable) log.debug "Sending ${myJSON} to ${serverIP}:${serverPort}"
        def headers = [:]
        headers.put("HOST", "${serverIP}:${serverPort}")
        headers.put("Content-Type", "application/json")
        def method = "POST"
        def path = "/cast"
        def result = new hubitat.device.HubAction(
            method: method,
            path: path,
            body: myJSON,
            headers: headers
        )
        return result
    } catch (Exception e) {
        log.error "Error = ${e}"
    } 
}

def castContent(type, source){
    castContent(defaultDevice, type, source)
}

def stop(String castDevice){
    if(logEnable)log.debug "Stopping ${castDevice}"
    def myJSON = "{ \"device\": \"${castDevice}\" }"
    try {
        if (logEnable) log.debug "Sending ${myJSON} to ${serverIP}:${serverPort}"
        def headers = [:]
        headers.put("HOST", "${serverIP}:${serverPort}")
        headers.put("Content-Type", "application/json")
        def method = "POST"
        def path = "/cast/stop"
        def result = new hubitat.device.HubAction(
            method: method,
            path: path,
            body: myJSON,
            headers: headers
        )
        return result
    } catch (Exception e) {
        log.error "Error = ${e}"
    } 
}

def stop(){
    if(defaultDevice){
        if(logEnable)log.debug "Stopping default device."
        stop(defaultDevice)
    }else{
        log.warn "No default device speficied. Sepcify device in command parameter or driver properties."
    }
}

def uninstalled(){
    children = getChildDevices()
    children.each{it->
        deleteChildDevice(it.deviceNetworkId)
    }
    log.debug "All children devices removed."
}
