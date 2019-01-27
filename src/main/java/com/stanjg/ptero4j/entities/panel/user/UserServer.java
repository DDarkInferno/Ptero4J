package com.stanjg.ptero4j.entities.panel.user;

import com.stanjg.ptero4j.PteroUserAPI;
import com.stanjg.ptero4j.actions.users.GenericUserAction;
import com.stanjg.ptero4j.entities.objects.server.FeatureLimits;
import com.stanjg.ptero4j.entities.objects.server.PowerAction;
import com.stanjg.ptero4j.entities.objects.server.PowerState;
import com.stanjg.ptero4j.entities.objects.server.ServerLimits;
import com.stanjg.ptero4j.entities.objects.server.ServerUsage;
import com.stanjg.ptero4j.util.HTTPMethod;

import okhttp3.Response;

import java.io.IOException;

import org.json.JSONObject;

public class UserServer {

    private PteroUserAPI api;
    private String id, uuid, name, description;
    boolean owner;
    private ServerLimits limits;
    private FeatureLimits featureLimits;

    public UserServer(PteroUserAPI api, JSONObject json) {
        this(
                api,
                json.getString("identifier"),
                json.getString("uuid"),
                json.getString("name"),
                json.getString("description"),
                json.getBoolean("server_owner"),
                new ServerLimits(
                        json.getJSONObject("limits")
                ),
                new FeatureLimits(
                        json.getJSONObject("feature_limits")
                )
        );
    }

    private UserServer(PteroUserAPI api, String id, String uuid, String name, String description, boolean owner, ServerLimits limits, FeatureLimits featureLimits) {
        this.api = api;
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.limits = limits;
        this.featureLimits = featureLimits;
    }

    public boolean sendCommand(String command) {
        return new GenericUserAction(api, "/servers/"+this.id+"/command", HTTPMethod.POST, new JSONObject().put("command", command)).execute() == 204;
    }

    public boolean start() {
        return sendPowerAction(PowerAction.START);
    }

    public boolean stop() {
        return sendPowerAction(PowerAction.STOP);
    }

    public boolean restart() {
        return sendPowerAction(PowerAction.RESTART);
    }

    public boolean kill() {
        return sendPowerAction(PowerAction.KILL);
    }

    public boolean sendPowerAction(PowerAction action) {
        return new GenericUserAction(api, "/servers/"+this.id+"/power", HTTPMethod.POST, new JSONObject().put("signal", action.getValue())).execute() == 204;
    }
    
    /**
     * 
     * @return Current power state of the Server, returns PowerState.ERROR if request errors
     */
    public PowerState getPowerState() {
    	try {
			Response response = api.getServersController().makeApiCall("/servers/"+this.id+"/utilization", HTTPMethod.GET);
            JSONObject json = new JSONObject(response.body().string());
            if(json.has("attributes")) json = json.getJSONObject("attributes");
            else return PowerState.ERROR;
            if(!json.has("state")) return PowerState.ERROR;
            switch(json.getString("state")) {
            case "on":
            	return PowerState.ON;
            case "off":
            	return PowerState.OFF;
            case "starting":
            	return PowerState.STARTING;
            case "stopping":
            	return PowerState.STOPPING;
            }
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return PowerState.ERROR;
    	
		
    }
    public String getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOwner() {
        return owner;
    }

    public ServerLimits getLimits() {
        return limits;
    }

    public FeatureLimits getFeatureLimits() {
        return featureLimits;
    }
    /**
     * 
     * @return Server usage or null if request errors
     */
    public ServerUsage getServerUsage() {
    	try {
    	Response response = api.getServersController().makeApiCall("/servers/"+this.id+"/utilization", HTTPMethod.GET);
        JSONObject json = new JSONObject(response.body().string());
        if(json.has("attributes")) {
        	json = json.getJSONObject("attributes");
        	if(json.has("memory")&&json.has("disk")&&json.has("cpu")) return new ServerUsage( Math.round((json.getJSONObject("cpu").getFloat("current")/json.getJSONObject("cpu").getFloat("limit"))*100), json.getJSONObject("memory").getInt("current"), json.getJSONObject("disk").getInt("current"));
        	else return null;
        }else {
        	System.err.println(json);
        	return null;
        }
    	}catch (Exception e) {
    		return null;
		}
    }
    
}
