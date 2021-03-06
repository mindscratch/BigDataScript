package ca.mcgill.mcb.pcingola.bigDataScript.cluster.host;

import ca.mcgill.mcb.pcingola.bigDataScript.cluster.Cluster;

/**
 * Local host information
 *
 * @author pcingola@mcgill.ca
 */
public class HostSsh extends Host {

	HostHealth health;

	public HostSsh(Cluster cluster, String hostName) {
		super(cluster, hostName);

		// Set basic parameters
		resources.setCpus(1);
		health.setAlive(true);
		health = new HostHealth(this);
	}

	public HostHealth getHealth() {
		return health;
	}

	@Override
	public boolean isAlive() {
		return getHealth().isAlive();
	}

}
