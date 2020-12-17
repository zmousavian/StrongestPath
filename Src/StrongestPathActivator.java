import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StrongestPathActivator extends AbstractCyActivator {

    @Override
    public void start(BundleContext bc) throws Exception {
        CySwingAppAdapter adapter = getService(bc, CySwingAppAdapter.class);
        CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
        CyNetworkViewManager cyNetworkViewManager = getService(bc, CyNetworkViewManager.class);
        CyNetworkViewFactory cyNetworkViewFactory = getService(bc, CyNetworkViewFactory.class);
        CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);
        CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
        VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
        CyEventHelper cyEventHelper = getService(bc, CyEventHelper.class);

        new MyStrongestPathPlugin(adapter, cyApplicationManager, cyNetworkViewManager, cyNetworkViewFactory,
                cyNetworkFactory, cyNetworkManager, cyEventHelper, visualStyleFactory);
    }
}
