package org.elasticsearch.plugin.helloworld;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.action.helloworld.HelloWorldAction;

public class HelloWorldPlugin extends AbstractPlugin {

    public String name() {
        return "hello-world";
    }

    public String description() {
        return "Hello World Plugin";
    }

    @Override public void processModule(Module module) {
        if (module instanceof RestModule) {
            ((RestModule) module).addRestAction(HelloWorldAction.class);
        }
    }
}
