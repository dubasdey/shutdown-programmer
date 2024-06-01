package com.github.dubasdey;

import java.io.IOException;

public final class OS {
    
    public static void cancel(){
        try {
            Runtime.getRuntime().exec("shutdown /a");
        } catch (IOException e) {
            // Ignore
            e.printStackTrace();
        }
    }
    
    public static ActionModel[] allowedActions(){
        return new ActionModel[]{
            new ActionModel("Hibernate","shutdown /h"),
            new ActionModel("Shutdown","shutdown /p /f"),
            new ActionModel("Hybrid","shutdown /s /f /hybrid")
        };
    }

    public static void executeAction(ActionModel action){
        try {
            Runtime.getRuntime().exec(action.getAction());
        } catch (IOException e) {
            // Ignore
            e.printStackTrace();
        }
    }
}