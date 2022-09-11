package eu.gpirro.utilities;

public class Constants {

	public static final long TIMEOUT = 3000; // this can slow down querying, if
												// too high! /with 2000 it works
	// INCREASED to 6000 for FreeBAseExperiments (25 billions of triples!); for
	// dbpedia works with 2000; also for Fb with distance 2!

	// with 15000 for distances >=5
	/**
	 * The following parameters can be read from a file!
	 */

	public static int TOTAL_NUMER_OF_TRIPLES = 825761509;
	public static final long CONNECTION_DELAY = 700;

	/**
	 * 
	 * IMPORTANT PARAMETERS - BEGIN
	 * 
	 * 
	 */

	// FREEBASE NAMED GRAPH:
	// http://commondatastorage.googleapis.com/freebase-public/rdf/freebase-rdf-2013-11-17-00-00.gz
	// public static final String NAMED_GRAPH =
	// "http://commondatastorage.googleapis.com/freebase-public/rdf/freebase-rdf-2013-11-17-00-00.gz";
	// // Freebase
	// public static final String ENTPOINT_HTTP =
	// "http://lod.openlinksw.com/sparql";
	// %
	// %
	public static final String NAMED_GRAPH = ""; // Freebase
	public static final String ENTPOINT_HTTP = "http://dbpedia.org/sparql";
	// "http://dbpedia.west.uni-koblenz.de:8890/sparql";
	//

	// LOD_CLOUD= "http://lod.openlinksw.com/sparql";
	// DBpedia= "http://dbpedia.org/sparql";
	// LinkedMDB= "http://data.linkedmdb.org/snorql/";
	//
	
	
	public static final int MAX_DIST = 2; // the distance is MAX_DIST +1
	
	public static final int TOP_K = 2;
	public static final double RADIUS_DELTA = 0.5;
	public static final int NUM_PARALLEL_QUERIES = 10;
	// Runtime.getRuntime().availableProcessors(); // 10 works good
	// generate a file for each type of explanation
	public static final boolean WRITE_EXPLANATIONS_ON_DISK = false;
	// get the PFITF from a remote graph
	public static final boolean REMOTE_ITF_VALUES = true;
	public static final int NUM_ITF_THREADS = 15;
	
	/**
	 * 
	 Thea:<http://rdf.freebase.com/ns/m.02jgwn> % % 
	 Friz:<http://rdf.freebase.com/ns/m.032md>
	 LDMB: <http://data.linkedmdb.org/resource/director/8411> Fritz
	 LMDB <http://data.linkedmdb.org/resource/writer/14556> Thea
	 * 
	 * <http://dbpedia.org/resource/Fritz_Lang>,<http://dbpedia.org/resource/
	 * Thea_von_Harbou> IMPORTANT PARAMETERS - END
	 */
	
	public static final String PATH_STATS_RES = "path-stats-res";
	public static final String EXPL_STATS_RES = "expl-stats-res";

	public static final String DATASET_DIRECTORY = "./DATASETS/";
	public static final String RESULTS_DIRECTORY = "./DATASETS/RESULTS/";

	// constants used to retrive results to be written in a file;
	public static final String RES_TOTAL_TIME_PATHS = "total-time-paths";
	public static final String RES_TOTAL_NUM_QUERIES_PATHS = "total-num-queries-paths";
	public static final String RES_TOTAL_NUM_QUERIES_WITH_RES_PATHS = "total-num-queries-with-res-paths";
	public static final String RES_TOTAL_NUM_PATHS = "total-num-paths";
	public static String TIME_EXPL_MERGE = "time-expl-merge";

	public static String ALL_PATTERNS = "all-patterns";
	public static String TIME_EXPL_MERGE_PATTERNS = "total-time-patterns";
	public static String TOTAL_NUM_EXPL_PATTERNS = "total-num-patterns";
	// public static String
	// TOTAL_SIZE_MERGE_EXPL_PATTERNS="size-expl-merge-patterns";

	public static String SIZE_MIP = "size-mip";
	public static String TIME_MIP = "time-mip";
	public static String MIP = "mip";

	public static String TOPK = "topk";
	public static String TIME_TOPK = "time-topk";
	public static String SIZE_TOPK = "size-topk";

	public static String TOPK_PATTERNS = "topk-patterns";
	public static String TIME_TOPK_PATTERNS = "time-topk-patterns";
	public static String SIZE_TOPK_PATTERNS = "size-topk-patterns";

	public static String SIZE_DIVERSITY = "size-diversity";
	public static String TIME_DIVERSITY = "time-diversity";
	public static String DIVERSITY = "diversity";

	/**
	 * The above parameters can be read from a file!
	 */

	// /
	public static String DIRECTION_OUT = "fw";
	public static String DIRECTION_IN = "bw";
	public static String DIRECTION_FW = "forward";
	public static String DIRECTION_BW = "backward";

	public static String PREFIXES = "";
	// "PREFIX dbpedia: <http://dbpedia.org/resource/> ";

	
	 /* public static String PREFIXES =
	  "PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
	  "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
	  "PREFIX  owl: <http://www.w3.org/2002/07/owl#> \n" +
	  "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
	  "PREFIX  foaf: <http://xmlns.com/foaf/0.1/> \n" +
	  "PREFIX  dc: <http://purl.org/dc/elements/1.1/> \n" +
	  "PREFIX  dbpedia2: <http://dbpedia.org/property/> \n" +
	  "PREFIX  skos: <http://www.w3.org/2004/02/skos/core#> \n";
	  */
	 

	// COMMON NAMESPACES
	public static final String DBP_NS = "http://dbpedia.org/resource/";
	public static final String WORDNET_NS = "http://www.w3.org/2006/03/wn/wn20/instances/";
	public static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
	public static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String RDF_BLANK_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nodeID=";
	public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
	public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
	public static final String DCTERMS_NS = "http://purl.org/dc/terms/";
	public static final String DCELEM_NS = "http://purl.org/dc/elements/1.1/";

	public static final String DBPEDIA_PROP_NS = "http://dbpedia.org/property/";
	public static final String DBPEDIA_ONTO_NS = "http://dbpedia.org/ontology/";

	public static final String GEO_NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
	public static final String GEO_RSS_NS = "http://www.georss.org/georss/";
	public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
	public static final String SCHOLAR_NS = "http://scholar.google.com/";
	public static final String UMBEL_NS = "http://umbel.org/umbel/rc/";
	public static final String SCHEMA_NS = "http://schema.org/";

	public static final String SCHOLAR_SEARCH_NS = "http://scholar.google.com/scholar?q=";

	public static final String[] namespaces_values = { SCHEMA_NS, UMBEL_NS,
			WORDNET_NS, DBP_NS, DBPEDIA_ONTO_NS, OWL_NS, RDF_BLANK_NS, RDF_NS,
			RDFS_NS, SKOS_NS, FOAF_NS, DCTERMS_NS, DCELEM_NS, DBPEDIA_PROP_NS,
			DBPEDIA_ONTO_NS, GEO_NS, GEO_RSS_NS, XSD_NS, SCHOLAR_SEARCH_NS,
			SCHOLAR_NS };

	public static final String SCHEMA = "schema:";

	public static final String DBPEDIA = "dbp:";
	public static final String OWL = "owl:";
	public static final String RDF = "rdf:";
	public static final String RDF_BLANK = "_:";
	public static final String RDFS = "rdfs:";
	public static final String SKOS = "skos:";
	public static final String FOAF = "foaf:";
	public static final String DCTERMS = "dc:";
	public static final String DCELEM = "dce:";
	public static final String DBPEDIA_ONTO = "dbpedia:";
	public static final String DBPEDIA_PROP = "dbpprop:";
	public static final String DBPEDIA_ONTO_OWL = "dbpediaowl:";

	public static final String UMBEL = "umbel:";

	public static final String GEO = "geo:";
	public static final String GEO_RSS = "georss:";
	public static final String XSD = "xsd:";
	public static final String SCHOLAR = "scholar:";
	public static final String SCHOLAR_QUERY = "scholar-q:";
	public static final String WORDNET = "wordnet:";

	public static final String[] namespaces_keys = { SCHEMA, UMBEL, WORDNET,
			DBPEDIA, DBPEDIA_ONTO_OWL, OWL, RDF_BLANK, RDF, RDFS, SKOS, FOAF,
			DCTERMS, DCELEM, DBPEDIA_PROP, DBPEDIA_ONTO, GEO, GEO_RSS, XSD,
			SCHOLAR_QUERY, SCHOLAR };

}
