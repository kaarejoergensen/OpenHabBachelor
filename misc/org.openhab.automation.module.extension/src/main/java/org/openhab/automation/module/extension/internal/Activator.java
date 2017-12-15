/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.automation.module.extension.internal;

import org.openhab.automation.module.extension.internal.handler.ModuleExtensionHandlerFactory;
import org.openhab.automation.module.extension.internal.type.TypeProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Kåre Jørgensen - Initial Contribution
 *
 */
public class Activator implements BundleActivator {

    private TypeProvider mtProvider;
    private ModuleExtensionHandlerFactory handlerFactory;

    @Override
    public void start(BundleContext context) throws Exception {
        mtProvider = new TypeProvider();
        mtProvider.register(context);

        handlerFactory = new ModuleExtensionHandlerFactory(context);
        handlerFactory.register(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        mtProvider.unregister();
        mtProvider = null;

        handlerFactory.unregister();
        handlerFactory = null;
    }

}
