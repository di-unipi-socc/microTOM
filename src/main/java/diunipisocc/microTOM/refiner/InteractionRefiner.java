package diunipisocc.microTOM.refiner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import diunipisocc.microTOM.graph.Edge;
import diunipisocc.microTOM.graph.JsService;

/**
 * This class is designed for performing interaction refinement
 * @author javad khalili
 *
 */

public class InteractionRefiner{

	/**
	 * Set names of source and target node on both end of each edge
	 * @param edges List of Edge objects created by parsing edge elements present in graph data JSON file generated by Kiali
	 * @param services List of JsService objects created by parsing node elements present in graph data JSON file generated by Kiali 
	 */
	public void edgeNameSetter(List<Edge> edges, List<JsService> services) {
		// Naming Sources of edges
		for (Edge edge : edges) {
			for (JsService service : services) {
				if (edge.getSourceId().equals(service.getId())) {
					edge.setSourceName(service.getServiceName());
					break;
				}
			}
		}

		// Naming targets of edges
		for (Edge edge : edges) {
			for (JsService service : services) {
				if (edge.getTargetId().equals(service.getId())) {
					edge.setTargetName(service.getServiceName());
					break;
				}
			}
		}
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("List of Edges in service mesh!");
		System.out.println("-------------------------------------------------------------------------------");
		edges.forEach(System.out::println);
		System.out.println();
		//return edges;
	}

	
	/**
	 * Assign boolean properties related to each interaction in microTOSCA topology graph of application based on Istio traffic management configurations applied to a service
	 * @param services List JsService objects which represent envoy proxies integrated by Kubernetes services
	 * @param edges List of Edge objects
	 * @param files A containing Istio traffic management configurations
	 */
	public void labelEdges(List<JsService> services, List<Edge> edges, List<File> files) { // files are destination
		// rules
		// and virutal services
// this for loop is to check whether the service is a circuit breaker or not
// DeploymentMapper dm = new DeploymentMapper();
		for (JsService service : services) {
// Checking if the service is a circuit breaker.
			if (service.isHasCB()) {
// DestinationRule dr = null;
// going throgh Istio destination rules and virtual service files
				for (File file : files) {
// if the file a destination rule
					if (isDestinationRuleFile(file)) {
// if the destination rule is related to this service
						if (isNameMatching(file, service.getServiceName())) {
// if the connectionPool value is assigned
							if (hasConnectionPool(file)) {
// label all the edges incoming to this service as 'c'
								for (Edge edge : edges) {
									if (edge.getTargetId().equals(service.getId())) {
										edge.setC("c");
									}
								}
							}
						}

					}
				}

			}

		}

// this for loop is to check whether the service has timeout or not
		for (JsService service : services) {
			if (service.isHasVS()) {

				for (File file : files) {
					if (isVirtualServiceFile(file)) {
						if (isNameMatching(file, service.getServiceName())) {
							if (hasTimeout(file)) {
								for (Edge edge : edges) {
									if (edge.getTargetId().equals(service.getId())) {
										edge.setT("t");
									}
								}
							}
						}

					}
				}

			}
		}

	}

// ----------------------------------------------------------------------------------------------
	/**
	 * Checks for Istio DestinationRule configuration.
	 * @param  file A file to be checked by applying the conditions defined.
	 * @return True if the file is a Istio DestinationRule configuration definition object file, false otherwise.
	 */
	// To find the Destination Rule of a specific service
	private boolean isDestinationRuleFile(File file) {
		BufferedReader br = null; // Reading one file (used for destination rule and virtual service object
// creation)
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
// System.out.println(line);
				if (line.contains("kind: DestinationRule")) {
					br.close();
					return true;
				}
				line = br.readLine();
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

// -----------------------------------------------------------------------------------------
	/**
	 * Checks for Istio VirtualService configuration.
	 * @param  file A file to be checked by applying the conditions defined.
	 * @return True if the file is a Istio VirtualService configuration definition object file, false otherwise.
	 */
// to check whether the file is a virtual service file or not
	private boolean isVirtualServiceFile(File file) {
		BufferedReader br = null; // Reading one file (used for destination rule and virtual service object
// creation)
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
// System.out.println(line);
				if (line.contains("kind: VirtualService")) {
					br.close();
					return true;
				}
				line = br.readLine();
			}
			br.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return false;
	}

// --------------------------------------------------------------------------------------------
	
	/**
	 * Checks for matching service which the Istio traffic management policies applied on.
	 * @param file An Istio traffic management configuration file.
	 * @param servieName String serviceName
	 * @return True if it matches, false otherwise.
	 */
	
	
// to check whether the current file is for current service or not
	private boolean isNameMatching(File file, String serviceName) {
		BufferedReader br = null; // Reading one file (used for destination rule and virtual service object
// creation)
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				if (line.contains("host: " + serviceName)) {
					br.close();
					return true;
				}
				line = br.readLine();
			}

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		return false;
	}

// ----------------------------------------------------------------------------------------------
	/**
	 * Checks for circuit breaking configuration for a service
	 * @param file An Istio traffic management configuration file.
	 * @return True if related information regarding configuring circuit breaking exist, false otherwise.
	 */
	
	
// to check whether the file has "connectionPool" field
	private boolean hasConnectionPool(File file) {
		BufferedReader br = null; // Reading one file (used for destination rule and virtual service object
// creation)
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
// System.out.println(line);
				if (line.contains("connectionPool:") || (line.contains("outlierDetection:"))) {
					br.close();
					return true;
				}
				line = br.readLine();
			}
			br.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

// ------------------------------------------------------------------------------------------------
	/**
	 * Checks for request timeout configuration for a service
	 * @param file An Istio traffic management configuration file.
	 * @return True if related information regarding configuring request timeout exist, false otherwise.
	 */
	
// to check whether the service has timeout or not
	private boolean hasTimeout(File file) {
		BufferedReader br = null;
		String[] words = null;// Reading one file (used for destination rule and virtual service object
// creation)
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
// System.out.println(line);
				words = line.split(" ");
				for (String word : words) {
					if (word.contains("timeout:")) {
						br.close();
						return true;
					}
				}
				line = br.readLine();
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
