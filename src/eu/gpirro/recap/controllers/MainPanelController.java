package eu.gpirro.recap.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.json.JSONException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.yworks.util.FlagsEnum;
import com.yworks.yfiles.algorithms.GraphConnectivity;
import com.yworks.yfiles.algorithms.INodeMap;
import com.yworks.yfiles.algorithms.Node;
import com.yworks.yfiles.animation.AnimationHandler;
import com.yworks.yfiles.animation.Animator;
import com.yworks.yfiles.canvas.GraphControl;
import com.yworks.yfiles.canvas.GraphOverviewControl;
import com.yworks.yfiles.canvas.ScrollBarVisibility;
import com.yworks.yfiles.drawing.DefaultArrow;
import com.yworks.yfiles.drawing.ExteriorLabelModel;
import com.yworks.yfiles.drawing.ILabelStyle;
import com.yworks.yfiles.drawing.INodeStyle;
import com.yworks.yfiles.drawing.ImageNodeStyle;
import com.yworks.yfiles.drawing.PolylineEdgeStyle;
import com.yworks.yfiles.drawing.RotatedSliderEdgeLabelModel;
import com.yworks.yfiles.drawing.SimpleLabelStyle;
import com.yworks.yfiles.geometry.IPoint;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.GraphItemTypes;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.LayoutExtensions;
import com.yworks.yfiles.graph.LayoutGraphAdapter;
import com.yworks.yfiles.input.GraphEditorInputMode;
import com.yworks.yfiles.input.GraphSnapContext;
import com.yworks.yfiles.input.IInputModeContext;
import com.yworks.yfiles.input.IPositionHandler;
import com.yworks.yfiles.layout.CopiedLayoutGraph;
import com.yworks.yfiles.layout.hierarchic.IncrementalHierarchicLayouter;
import com.yworks.yfiles.layout.organic.InteractiveOrganicLayouter;
import com.yworks.yfiles.layout.organic.ShuffleLayouter;
import com.yworks.yfiles.model.IModelItem;
import com.yworks.yfiles.system.Pen;
import com.yworks.yfiles.system.TextFormat;

import eu.gpirro.explanation.multithread.RecapMultiThread;
import eu.gpirro.explanation.structures.Path;
import eu.gpirro.utilities.Constants;
import eu.gpirro.utilities.GetWikiPage;

public class MainPanelController {

	/**
	 * Custom position handler that automatically triggers layout updates when
	 * graph elements have been moved interactively.
	 */
	private class MyPositionHandler implements IPositionHandler {
		private final IPositionHandler originalHandler;

		public MyPositionHandler(IPositionHandler originalHandler) {
			this.originalHandler = originalHandler;
		}

		@Override
		public void cancelDrag(IInputModeContext inputModeContext,
				PointD originalLocation) {
			originalHandler.cancelDrag(inputModeContext, originalLocation);
			InteractiveOrganicLayouter layouter = MainPanelController.this.layouter;
			if (layouter != null) {
				CopiedLayoutGraph copy = copiedLayoutGraph;
				for (INode node : graphExplanationControl.getSelection()
						.getSelectedNodes()) {
					Node copiedNode = copy.getCopiedNode(node);
					if (copiedNode != null) {
						// update the position of the node in the CLG to match
						// the one in the IGraph
						layouter.setCenter(copiedNode, node.getLayout()
								.getCenter().getX(), node.getLayout()
								.getCenter().getY());
						layouter.setStress(copiedNode, 0);
					}
				}
				for (Node copiedNode : copy.getNodes()) {
					// reset the node's inertia to be fixed
					layouter.setInertia(copiedNode, 1.0);
					layouter.setStress(copiedNode, 0);
				}
				// we don't want to restart the layout (since we canceled the
				// drag anyway...)
			}
		}

		@Override
		public void dragFinished(IInputModeContext inputModeContext,
				PointD originalLocation, PointD newLocation) {
			originalHandler.dragFinished(inputModeContext, originalLocation,
					newLocation);
			InteractiveOrganicLayouter layouter = MainPanelController.this.layouter;
			if (layouter != null) {
				CopiedLayoutGraph copy = copiedLayoutGraph;
				for (INode node : graphExplanationControl.getSelection()
						.getSelectedNodes()) {
					Node copiedNode = copy.getCopiedNode(node);
					if (copiedNode != null) {
						// update the position of the node in the CLG to match
						// the one in the IGraph
						layouter.setCenter(copiedNode, node.getLayout()
								.getCenter().getX(), node.getLayout()
								.getCenter().getY());
						layouter.setStress(copiedNode, 0);
					}
				}
				for (Node copiedNode : copy.getNodes()) {
					// reset the node's inertia to be fixed
					layouter.setInertia(copiedNode, 1.0);
					layouter.setStress(copiedNode, 0);
				}
			}
		}

		@Override
		public IPoint getLocation() {
			return originalHandler.getLocation();
		}

		@Override
		public boolean handleMove(IInputModeContext inputModeContext,
				PointD originalLocation, PointD newLocation) {
			boolean b = originalHandler.handleMove(inputModeContext,
					originalLocation, newLocation);
			InteractiveOrganicLayouter layouter = MainPanelController.this.layouter;
			if (layouter != null) {
				CopiedLayoutGraph copy = copiedLayoutGraph;
				for (INode node : graphExplanationControl.getSelection()
						.getSelectedNodes()) {
					Node copiedNode = copy.getCopiedNode(node);
					if (copiedNode != null) {
						// update the position of the node in the
						// CopiedLayoutGraph to match the one in the IGraph
						layouter.setCenter(copiedNode, node.getLayout()
								.getCenter().getX(), node.getLayout()
								.getCenter().getY());
						// increasing the heat has the effect that the layouter
						// will consider these nodes as not completely placed...
						increaseHeat(copiedNode, layouter, 0.05);
					}
				}
				// notify the layouter that there is new work to do...
				layouter.wakeUp();
			}
			return b;
		}

		private void increaseHeat(Node copiedNode,
				InteractiveOrganicLayouter layouter, double delta) {
			// increase heat of neighbors
			for (Node neighbor : copiedNode.getNeighbors()) {
				double oldStress = layouter.getStress(neighbor);
				layouter.setStress(neighbor, Math.min(1, oldStress + delta));
			}
		}

		@Override
		public void initializeDrag(IInputModeContext inputModeContext) {
			InteractiveOrganicLayouter layouter = MainPanelController.this.layouter;

			if (layouter != null) {
				CopiedLayoutGraph copy = copiedLayoutGraph;
				INodeMap componentNumber = copy.createNodeMap();
				GraphConnectivity.connectedComponents(copy, componentNumber);
				Set<Integer> movedComponents = new HashSet<>();
				Set<Node> selectedNodes = new HashSet<>();
				for (INode node : graphExplanationControl.getSelection()
						.getSelectedNodes()) {
					Node copiedNode = copy.getCopiedNode(node);
					if (copiedNode != null) {
						// remember that we nailed down this node
						selectedNodes.add(copiedNode);
						// remember that we are moving this component
						movedComponents.add(componentNumber.getInt(copiedNode));
						// update the position of the node in the
						// CopiedLayoutGraph to match the one in the IGraph
						layouter.setCenter(copiedNode, node.getLayout()
								.getCenter().getX(), node.getLayout()
								.getCenter().getY());
						// actually, the node itself is fixed at the start of a
						// drag gesture
						layouter.setInertia(copiedNode, 1.0);
						// increasing the heat has the effect that the layouter
						// will consider this node as not completely placed...
						// In this case, the node itself is fixed, but it's
						// neighbors will wake up
						increaseHeat(copiedNode, layouter, 0.5);
					}
				}

				// there are components that won't be moved - nail the nodes
				// down so that they don't spread apart infinitely
				for (Node copiedNode : copy.getNodes()) {
					if (!movedComponents.contains(componentNumber
							.getInt(copiedNode))) {
						layouter.setInertia(copiedNode, 1);
					} else {
						if (!selectedNodes.contains(copiedNode)) {
							// make it float freely
							layouter.setInertia(copiedNode, 0);
						}
					}
				}

				// dispose the map
				copy.disposeNodeMap(componentNumber);
				// notify the layouter that there is new work to do...
				layouter.wakeUp();
			}
			originalHandler.initializeDrag(inputModeContext);
		}

		@Override
		public void setPosition(PointD location) {
			originalHandler.setPosition(location);
		}
	}

	/**
	 * Inner class to write results in a table
	 * 
	 * @author gpirro
	 *
	 */
	public static class Result {

		private final SimpleStringProperty first = new SimpleStringProperty();
		private final SimpleStringProperty second = new SimpleStringProperty();

		public String getFirst() {
			return first.get();
		}

		public String getSecond() {
			return second.get();
		}

	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private TextField targetEntityTextField;

	@FXML
	private TextField sourceEntityTextField;

	@FXML
	private Button runExplanationButton;

	@FXML
	private Font x1;

	@FXML
	private Color x2;

	@FXML
	private Font x3;

	@FXML
	private Color x4;

	@FXML
	private ProgressIndicator progressBarInitial;

	@FXML
	private Tab firstTabMain;

	@FXML
	private Tab queryingTab;

	@FXML
	private Tab secondTabMain;

	@FXML
	private MenuItem menuQuit;

	@FXML
	private AnchorPane leftResultPanel;

	@FXML
	private AnchorPane centralResultPanel;

	@FXML
	private TabPane tabPaneGeneral;

	@FXML
	private Slider sliderPaths;

	@FXML
	private Slider sliderPatterns;

	@FXML
	private Slider sliderDiversity;

	@FXML
	private Slider sliderNumRes;

	@FXML
	private TextArea resTxtArea;

	private RecapMultiThread recap;

	private String source_entity;

	private String target_entity;

	@FXML
	private Button computeBasicButton;

	@FXML
	private Button buttonExecQuery;

	@FXML
	public WebView wikipage;

	@FXML
	public WebView webViewSource;

	@FXML
	public WebView webViewTarget;

	@FXML
	public GraphControl graphExplanationControl;

	@FXML
	public GraphControl graphQueryingControl;

	@FXML
	public Label s_entity_name;

	@FXML
	public Label t_entity_name;

	@FXML
	public ImageView source_ent_info;

	@FXML
	public ImageView target_ent_info;

	@FXML
	public GraphOverviewControl graphOverviewControl;

	@FXML
	private InteractiveOrganicLayouter layouter;

	@FXML
	private InteractiveOrganicLayouter layouterGraphQuery;

	@FXML
	private CopiedLayoutGraph copiedLayoutGraph;

	@FXML
	private CopiedLayoutGraph copiedLayoutGraphQuerying;

	@FXML
	private TableColumn<Result, String> first;

	@FXML
	private TableColumn<Result, String> second;

	@FXML
	private TableView<Result> tableStatistics;

	@FXML
	private TextField value_top_k_paths;

	@FXML
	private TextField value_top_k_patterns;

	@FXML
	private TextField valueNumRes;

	@FXML
	private TextField value_diversity;

	@FXML
	private TextArea txtAreaSPARQLQuery;

	@FXML
	private ListView<String> listEdges;

	@FXML
	private ListView<String> listResultsPaths;

	@FXML
	private ListView<String> listPredicatesInExpl;

	@FXML
	private ListView<String> listPatterns;

	@FXML
	private ListView<String> listPathInstances;

	@FXML
	private ListView<String> listOfResult;

	@FXML
	private ListView<String> listStatsQuerying;

	@FXML
	private Slider sliderDistance;
	@FXML
	private TextField txtFieldDistance;

	@FXML
	private Label labTypeExplan;

	@FXML
	private TextField txtGenericEndpointAddress;

	@FXML
	private TextField txtGenericNG;

	// keep all path patterns with associated instances
	private Hashtable<String, ArrayList<Model>> patterns_and_instances;

	// keep all path patterns with associated instances
	private Hashtable<String, Model> string_to_model_instances;

	private Model current_explanation;

	private int distance;

	/**
	 * 
	 */
	public MainPanelController() {

		recap = new RecapMultiThread();
		recap.setMainPanelController(MainPanelController.this);
		string_to_model_instances = new Hashtable<String, Model>();
	}

	@FXML
	void buttonExecQueryHandler(ActionEvent event) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// System.out.println(txtAreaSPARQLQuery.getText());
				try {
					ArrayList<String> res = recap
							.getExplanationBuilder()
							.getEntitySuggestion()
							.computeEntitySuggestions(
									txtAreaSPARQLQuery.getText(),
									txtGenericEndpointAddress.getText(),
									txtGenericNG.getText());
					ObservableList<String> all_res = FXCollections
							.observableArrayList();
					for (String pair : res) {
						all_res.add(pair);
					}

					listOfResult.setItems(all_res);

					System.out
							.println("Number of query results (entity suggestions)="
									+ all_res.size());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * Handles the event when the compute-basic explanation is clicked
	 * 
	 * @param event
	 */
	@FXML
	void computeBasicMergeButtonHandler(ActionEvent event) {

		/**
		 * check if already computed! do not recompute!
		 */
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (recap.getPaths() == null)
					System.out.println("No paths found!");

				Vector<Object> res_merge = recap.getExplanationBuilder()
						.BASICMERGE(source_entity, target_entity);

				Model basic_union_all = (Model) res_merge.get(0);

				// System.out.println("# of paths="+recap.getPaths().size());
				sliderPaths.setMax(Double.parseDouble(recap.getPaths().size()
						+ ""));

				paintExplanation(basic_union_all);

				labTypeExplan.setText("Merge of all paths Explanation");

				Hashtable<String, String> stats = recap.getExplanationBuilder()
						.getPathFindingStatistics();

				stats.put(Constants.TIME_EXPL_MERGE, res_merge.get(1) + "");

				writeStatisticsOnTable(stats, Constants.PATH_STATS_RES);

			}
		});

	}

	@FXML
	void computeMIPButtonHandler(ActionEvent event) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (recap.getPaths() == null)
					System.out.println("No paths found!");
				else {
					Vector<Object> res = recap.getExplanationBuilder()
							.MIP_LOCAL(source_entity, target_entity);
					Path p = (Path) res.get(0);
					Hashtable<String, String> stats = new Hashtable<String, String>();
					stats.put(Constants.TIME_MIP, res.get(1) + "");
					stats.put(Constants.SIZE_MIP, p.getModel().size() + "");
					stats.put(Constants.RES_TOTAL_NUM_PATHS, recap.getPaths()
							.size() + "");

					writeStatisticsOnTable(stats, Constants.MIP);

					paintExplanation(p.getModel());

					labTypeExplan.setText("Most Informative Path Explanation");

				}

			}
		});

	}

	@FXML
	void computePatternsHandler(ActionEvent event) {

		/**
		 * check if already computed! do not recompute!
		 */
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (recap.getPaths() == null)
					System.out.println("No paths found!");

				Vector<Object> res_merge = recap.getExplanationBuilder()
						.getPatternsAndInstances(source_entity, target_entity);

				patterns_and_instances = (Hashtable<String, ArrayList<Model>>) res_merge
						.get(0);

				// System.out.println("# of paths="+recap.getPaths().size());
				sliderPatterns.setMax(Double.parseDouble(recap
						.getExplanationBuilder().getPatterns().size()
						+ ""));

				/**
				 * 
				 * 
				 */
				Hashtable<String, String> stats = new Hashtable<String, String>();

				stats.put(Constants.TIME_EXPL_MERGE_PATTERNS, res_merge.get(1)
						+ "");
				stats.put(Constants.TOTAL_NUM_EXPL_PATTERNS,
						patterns_and_instances.size() + "");

				writeStatisticsOnTable(stats, Constants.ALL_PATTERNS);

				ObservableList<String> towrite = FXCollections
						.observableArrayList();

				towrite.add("Time Patterns: "
						+ stats.get(Constants.TIME_EXPL_MERGE_PATTERNS) + " ms");
				towrite.add("Total number of paths: " + recap.getPaths().size());
				towrite.add("Total number of patterns: "
						+ stats.get(Constants.TOTAL_NUM_EXPL_PATTERNS));

				listStatsQuerying.setItems(towrite);

				/**
				 * 
				 */

				ObservableList<String> all_patterns = FXCollections
						.observableArrayList();

				for (String pattern : patterns_and_instances.keySet()) {
					all_patterns.add(pattern);

				}

				listPatterns.setItems(all_patterns);

			}
		});

	}

	/**
	 * Main button, which controls the execution
	 * 
	 * @param event
	 */
	@FXML
	void explButtonClickHandler(ActionEvent event) {
		String source = sourceEntityTextField.getText();
		String target = targetEntityTextField.getText();

		recap.setSource(source);
		recap.setTarget(target);

		recap.setEndpointAddress(txtGenericEndpointAddress.getText());
		recap.setNamedGraph(txtGenericNG.getText());

		source_entity = source;
		target_entity = target;

		distance = Integer.parseInt(txtFieldDistance.getText());

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				recap.startComputingExplanations(distance);
				progressBarInitial.setVisible(true);

			}
		});

	}

	/**
	 * Convenience method to retrieve the graph.
	 */
	private IGraph getExplanationGraph() {
		return graphExplanationControl.getGraph();
	}

	private IGraph getQueryGraph() {
		return graphQueryingControl.getGraph();
	}

	/**
	 * Action handler for fit to content button action.
	 *
	 */
	@FXML
	public void handleExplGraphFitToContentAction() {
		graphExplanationControl.fitGraphBounds();
	}

	@FXML
	public void handleQueryGraphFitToContentAction() {
		graphQueryingControl.fitGraphBounds();
	}

	/**
	 * Action handler for zoom in button action.
	 *
	 */
	@FXML
	public void handleZoomInAction() {

		graphExplanationControl
				.setZoom(graphExplanationControl.getZoom() * 1.25);
	}

	/**
	 * Action handler for zoom in button action.
	 *
	 */
	@FXML
	public void handleZoomInActionQuery() {

		graphQueryingControl.setZoom(graphQueryingControl.getZoom() * 1.25);
	}

	/**
	 * Action handler for zoom out button action.
	 *
	 */
	@FXML
	public void handleZoomOutAction() {
		graphExplanationControl
				.setZoom(graphExplanationControl.getZoom() * 0.8);
	}

	/**
	 * Action handler for zoom out button action.
	 *
	 */
	@FXML
	public void handleZoomOutActionQuery() {
		graphQueryingControl.setZoom(graphQueryingControl.getZoom() * 0.8);
	}

	@FXML
	void initialize() {
		assert sourceEntityTextField != null : "fx:id=\"sourceEntityTextField\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert progressBarInitial != null : "fx:id=\"progressBarInitial\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert runExplanationButton != null : "fx:id=\"runExplanationButton\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert secondTabMain != null : "fx:id=\"secondTabMain\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert menuQuit != null : "fx:id=\"menuQuit\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert leftResultPanel != null : "fx:id=\"leftResultPanel\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert targetEntityTextField != null : "fx:id=\"targetEntityTextField\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert firstTabMain != null : "fx:id=\"firstTabMain\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert queryingTab != null : "fx:id=\"queryingTab\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert x2 != null : "fx:id=\"x2\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert x3 != null : "fx:id=\"x3\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert centralResultPanel != null : "fx:id=\"centralResultPanel\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert x4 != null : "fx:id=\"x4\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderPaths != null : "fx:id=\"sliderPaths\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert resTxtArea != null : "fx:id=\"resTxtArea\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert computeBasicButton != null : "fx:id=\"computeBasicButton\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert centralResultPanel != null : "fx:id=\"centralResultPanel\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert wikipage != null : "fx:id=\"wikipage\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert first != null : "fx:id=\"first\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert second != null : "fx:id=\"second\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert tableStatistics != null : "fx:id=\"tableStatistics\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderPaths != null : "fx:id=\"sliderPaths\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderPatterns != null : "fx:id=\"sliderPatterns\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderDiversity != null : "fx:id=\"sliderDiversity\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderDistance != null : "fx:id=\"sliderDistance\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert txtFieldDistance != null : "fx:id=\"txtFieldDistance\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert value_top_k_paths != null : "fx:id=\"value_top_k_paths\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert value_top_k_patterns != null : "fx:id=\"value_top_k_patterns\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert value_diversity != null : "fx:id=\"value_diversity\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert source_ent_info != null : "fx:id=\"source_ent_info\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert target_ent_info != null : "fx:id=\"target_ent_info\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert t_entity_name != null : "fx:id=\"t_entity_name\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert s_entity_name != null : "fx:id=\"s_entity_name\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listEdges != null : "fx:id=\"listEdges\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listResultsPaths != null : "fx:id=\"listResultsPaths\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listPredicatesInExpl != null : "fx:id=\"listPredicatesInExpl\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listPatterns != null : "fx:id=\"listPatterns\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listPathInstances != null : "fx:id=\"listPathInstances\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert txtAreaSPARQLQuery != null : "fx:id=\"txtAreaSPARQLQuery\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert buttonExecQuery != null : "fx:id=\"buttonExecQuery\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listOfResult != null : "fx:id=\"listOfResult\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert sliderNumRes != null : "fx:id=\"sliderNumRes\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert valueNumRes != null : "fx:id=\"valueNumRes\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert listStatsQuerying != null : "fx:id=\"listStatsQuerying\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert tabPaneGeneral != null : "fx:id=\"tabPaneGeneral\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert webViewTarget != null : "fx:id=\"webViewTarget\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert webViewSource != null : "fx:id=\"webViewSource\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert labTypeExplan != null : "fx:id=\"labTypeExplan\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert txtGenericEndpointAddress != null : "fx:id=\"txtGenericEndpointAddress\" was not injected: check your FXML file 'MainPanel.fxml'.";
		assert txtGenericNG != null : "fx:id=\"txtGenericNG\" was not injected: check your FXML file 'MainPanel.fxml'.";

		txtFieldDistance.setText("2");
		//
		sourceEntityTextField.textProperty().addListener(
				(observable, oldValue, newValue) -> {

					if (newValue.length() > 3) {

						String[] _possibleSuggestions = { "Hey", "Hello",
								"Hello World", "Apple", "Cool", "Costa",
								"Cola", "Coca Cola" };
						Set<String> possibleSuggestions = new HashSet<>(Arrays
								.asList(_possibleSuggestions));

						TextFields.bindAutoCompletion(sourceEntityTextField,
								"Hey", "Hello", "Hello World", "Apple", "Cool",
								"Costa", "Cola", "Coca Cola");
					}
					System.out.println("live querying line 789 "
							+ newValue.toString());
					// TODO Auto-generated method stub

				});

		sourceEntityTextField
				.setOnAction((event) -> {
					String page = sourceEntityTextField.getText();
					page = page.substring(page.lastIndexOf("/") + 1,
							page.length() - 1);
					System.out.println("PAGE source=" + page);
					// System.out.println("DO SOMETHING WHEN CLICKING on source");

					Vector<String> res_wiki = GetWikiPage
							.get("http://en.wikipedia.org/wiki/" + page);

					if (res_wiki != null && res_wiki.size() == 2) {
						String html = res_wiki.get(0);

						if (html.length() < 5)
							System.out.println("No wiki page");
						webViewSource.getEngine().loadContent(html);

					}
				});

		// target

		targetEntityTextField.textProperty().addListener(
				(observable, oldValue, newValue) -> {

					if (newValue.length() > 3) {

						String[] _possibleSuggestions = { "Hey", "Hello",
								"Hello World", "Apple", "Cool", "Costa",
								"Cola", "Coca Cola" };
						Set<String> possibleSuggestions = new HashSet<>(Arrays
								.asList(_possibleSuggestions));

						TextFields.bindAutoCompletion(sourceEntityTextField,
								"Hey", "Hello", "Hello World", "Apple", "Cool",
								"Costa", "Cola", "Coca Cola");
					}
					System.out.println("live querying line 816 "
							+ newValue.toString());
					// TODO Auto-generated method stub

				});

		targetEntityTextField
				.setOnAction((event) -> {

					String page = targetEntityTextField.getText();
					page = page.substring(page.lastIndexOf("/") + 1,
							page.length() - 1);
					System.out.println("PAGE source=" + page);

					Vector<String> res_wiki = GetWikiPage
							.get("http://en.wikipedia.org/wiki/" + page);

					if (res_wiki != null && res_wiki.size() == 2) {
						String html = res_wiki.get(0);

						if (html.length() < 5)
							System.out.println("No wiki page");
						webViewTarget.getEngine().loadContent(html);

					}
				});

		//
		/**
		 * SLIDER LISTENERS
		 */

		sliderPaths.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				// System.out.println("Value READ="+newValue.intValue());
				value_top_k_paths.setText(newValue.intValue() + "");

			}
		});

		sliderDistance.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						// System.out.println("Value READ="+newValue.intValue());
						txtFieldDistance.setText(newValue.intValue() + "");

					}
				});

		sliderPatterns.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						// System.out.println("Value READ="+newValue.intValue());
						value_top_k_patterns.setText(newValue.intValue() + "");

					}
				});

		sliderNumRes.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				valueNumRes.setText(newValue.intValue() + "");

			}
		});

		sliderDiversity.valueProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue, Number newValue) {
						// System.out.println("Value READ="+newValue.doubleValue());
						value_diversity.setText(newValue.doubleValue() + "");

					}
				});

	}

	@FXML
	void listPatternsClickHandler(MouseEvent click) {

		if (click.getClickCount() == 2) {

			String pattern = listPatterns.getSelectionModel().getSelectedItem();

			ArrayList<Model> path_instances = patterns_and_instances
					.get(pattern);

			writePathIstances(path_instances);

			Model temp = ModelFactory.createDefaultModel();
			for (Model m : path_instances)
				temp = temp.union(m);

			paintQueryGraph(temp);

			pattern = pattern.replace(source_entity, "?vsource");
			pattern = pattern.replace(target_entity, "?vtarget");

			/**
			 * TO DO
			 * 
			 * replace all <?vX> with ?vX
			 */

			String query = "SELECT DISTINCT ?vsource ?vtarget WHERE { "
					+ pattern + " FILTER( ?vsource != ?vtarget) }";

			txtAreaSPARQLQuery.setText(query);

			ObservableList<String> towrite = FXCollections
					.observableArrayList();

			towrite.add("Number of path instances: " + path_instances.size());
			listStatsQuerying.setItems(towrite);

		}
	}

	@FXML
	void listInstancesClickHandler(MouseEvent click) {

		if (click.getClickCount() == 2) {

			String path_instance = listPathInstances.getSelectionModel()
					.getSelectedItem();

			try {

				if (string_to_model_instances.get(path_instance) == null) {
				} else {
					Model instance = string_to_model_instances
							.get(path_instance);

					paintQueryGraph(instance);

					String pattern = listPatterns.getSelectionModel()
							.getSelectedItem();
					pattern = pattern.replace(source_entity, "?vsource");
					pattern = pattern.replace(target_entity, "?vtarget");
					String query = "SELECT DISTINCT ?vsource ?vtarget WHERE { "
							+ pattern + " FILTER( ?vsource != ?vtarget) }";

					txtAreaSPARQLQuery.setText(query);

					ObservableList<String> towrite = FXCollections
							.observableArrayList();

					// towrite.add("Number of path instances: "
					// + patterns_and_instances
					// .get(pattern).size());

					listStatsQuerying.setItems(towrite);
				}
			} catch (NullPointerException ex) {

			}
		}
	}

	@FXML
	void menuQuitHandler(ActionEvent event) {
		System.exit(0);
	}

	/**
	 * Updates all description when another element of the graph gets focused.
	 * 
	 * 
	 */
	private void onCurrentItemChanged() {
		final IModelItem currentItem = graphExplanationControl.getCurrentItem();

		if (currentItem instanceof INode) {
			final INode node = (INode) currentItem;

			String lab = node.toString();
			String page = lab.substring(lab.indexOf(":") + 1);

			Vector<String> res_wiki = GetWikiPage
					.get("http://en.wikipedia.org/wiki/" + page);

			if (res_wiki != null && res_wiki.size() == 2) {
				String html = res_wiki.get(0);

				INodeStyle in = new ImageNodeStyle(res_wiki.get(1));
				getExplanationGraph().setStyle(node, in);

				source_ent_info.setImage(new Image(res_wiki.get(1)));
				target_ent_info.setImage(new Image(
						"eu/gpirro/recap/controllers/usericon_male1.png"));
				t_entity_name.setText("");
				s_entity_name.setText(node.toString());

				listEdges.setVisible(false);
				target_ent_info.setVisible(false);
				t_entity_name.setVisible(false);

				if (html.length() < 5)
					System.out.println("No wiki page");
				wikipage.getEngine().loadContent(html);
			} else {

				source_ent_info.setImage(new Image(
						"eu/gpirro/recap/controllers/usericon_male1.png"));
				s_entity_name.setText(node.toString());

			}

		} else if (currentItem instanceof IEdge)

		{
			final IEdge edge = (IEdge) currentItem;

			final INode source = (INode) edge.getSourcePort().getOwner();
			final INode target = (INode) edge.getTargetPort().getOwner();

			listEdges.setVisible(true);
			target_ent_info.setVisible(true);
			t_entity_name.setVisible(true);

			String lab1 = source.toString();
			String lab2 = target.toString();

			String page = lab1.substring(lab1.indexOf(":") + 1);
			Vector<String> res_wiki = GetWikiPage
					.get("http://en.wikipedia.org/wiki/" + page);

			if (res_wiki != null && res_wiki.size() == 2) {
				INodeStyle in = new ImageNodeStyle(res_wiki.get(1));
				getExplanationGraph().setStyle(source, in);

				source_ent_info.setImage(new Image(res_wiki.get(1)));
				s_entity_name.setText(source.toString());
			}

			else {

				source_ent_info.setImage(new Image(
						"eu/gpirro/recap/controllers/usericon_male1.png"));
				s_entity_name.setText(source.toString());

			}

			page = lab2.substring(lab2.indexOf(":") + 1);
			Vector<String> res_wiki2 = GetWikiPage
					.get("http://en.wikipedia.org/wiki/" + page);

			StringTokenizer st = new StringTokenizer(currentItem.toString(),
					"|");
			ObservableList<String> items = FXCollections.observableArrayList();

			while (st.hasMoreTokens()) {
				String t = st.nextToken().trim();
				if (!items.contains(t))
					items.add(t.trim());

			}
			listEdges.setItems(items);

			if (res_wiki2 != null && res_wiki2.size() == 2) {
				INodeStyle in = new ImageNodeStyle(res_wiki2.get(1));
				getExplanationGraph().setStyle(target, in);
				target_ent_info.setImage(new Image(res_wiki2.get(1)));
				t_entity_name.setText(target.toString());

			} else {

				target_ent_info.setImage(new Image(
						"eu/gpirro/recap/controllers/usericon_male1.png"));
				t_entity_name.setText(target.toString());

			}

		}

	}

	/**
	 * Called right after stage is loaded. In JavaFX, nodes don't have a width
	 * or height until the stage is displayed and the scene graph is calculated.
	 * As {@link #initialize()} is called right after a node is created, but
	 * before displayed, we have to update the view port later.
	 */
	public void onLoaded() {

		updateViewportExplanation();
	}

	/**
	 * Paint the merge explanation
	 */
	private void paintExplanation(Model explanation) {

		current_explanation = explanation;

		/**
		 * CLEAR THE VISUALIZATION BUT NOT the data
		 */

		graphExplanationControl.getGraph().clear();
		/**
		 * 
		 */
		// set up input modes
		final GraphEditorInputMode graphViewerInputMode = new GraphEditorInputMode();
		graphViewerInputMode.setToolTipItems(GraphItemTypes.LABELED_ITEM);
		graphViewerInputMode.setClickableItems(FlagsEnum.or(

		GraphItemTypes.NODE, GraphItemTypes.EDGE));

		graphViewerInputMode.setFocusableItems(FlagsEnum.or(
				GraphItemTypes.NODE, GraphItemTypes.EDGE));

		graphViewerInputMode.setSelectableItems(FlagsEnum.or(
				GraphItemTypes.NODE, GraphItemTypes.EDGE));

		graphViewerInputMode.setSnapContext(new GraphSnapContext());

		// do not allow to create new nodes/edges
		graphViewerInputMode.getCreateEdgeInputMode().setEnabled(false);
		graphViewerInputMode.setNodeCreationAllowed(false);

		graphViewerInputMode.setMovableItems(FlagsEnum.or(GraphItemTypes.NODE,
				GraphItemTypes.EDGE));

		// handles the movement of nodes/edges
		graphViewerInputMode.getMoveInputMode().setPositionHandler(
				new MyPositionHandler(graphViewerInputMode.getMoveInputMode()
						.getPositionHandler()));

		// Populates the graph and override some styles and label models
		paintExplanationGraph(explanation);

		// create a copy of the graph for the layout algorithm
		LayoutGraphAdapter adapter = new LayoutGraphAdapter(
				graphExplanationControl.getGraph());
		copiedLayoutGraph = new CopiedLayoutGraph(adapter, adapter);

		layouter = startLayouter();

		wakeUp();

		graphExplanationControl.currentItemProperty().addListener(
				new ChangeListener<IModelItem>() {
					@Override
					public void changed(
							ObservableValue<? extends IModelItem> observableValue,
							IModelItem iModelItem, IModelItem iModelItem2) {
						onCurrentItemChanged();

					}
				});

		// Set the input mode
		graphExplanationControl.setInputMode(graphViewerInputMode);

		/**
		 * BEGIN - GRAPH OVERVIEW
		 */
		graphOverviewControl.setGraphControl(graphExplanationControl);
		graphOverviewControl
				.setHorizontalScrollBarPolicy(ScrollBarVisibility.NEVER);
		graphOverviewControl
				.setVerticalScrollBarPolicy(ScrollBarVisibility.NEVER);
		/**
		 * END - GRAPH OVERVIEW
		 */

		graphExplanationControl.fitGraphBounds();
	}

	// / BEGIN INNER CLASS

	/**
	 * Retruns a readable label
	 * 
	 * @param label
	 * @return
	 */
	private String getAbbreviatedLabel(String label) {

		String temp = label;
		if (label.contains("dbpedia")) {

			return temp = label.substring(label.indexOf(":") + 1);
		} else if (label.contains("freebase")) {

			return temp = label.substring(label.lastIndexOf("/") + 1);

		}
		return label;

	}

	/**
	 * Draw the Explanation
	 */
	private void paintExplanationGraph(Model explanation) {
		/**
		 * Node/Edge Style
		 */
		setExplanationGraphNodeEdgeStyle();

		ObservableList<String> all_edges = FXCollections.observableArrayList();
		/**
		 * DELETE OLD DATA OTHERWISE THEY WILL BE SHOWN IN THE NEW GRAPHS
		 */
		IGraph graph = getExplanationGraph();

		graph.clear();

		Hashtable<String, INode> nodes = new Hashtable<String, INode>();

		StmtIterator st = explanation.listStatements();

		while (st.hasNext()) {
			Statement stmt = st.nextStatement(); // get next statement

			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object
			String sub_string = subject.toString();

			String abbr_label_subj = getAbbreviatedLabel(sub_string);

			String pred_string = predicate.toString();
			String abbr_label_pred = getAbbreviatedLabel(pred_string);

			String obj_string = object.toString();
			String abbr_label_obj = getAbbreviatedLabel(obj_string);

			INode sub;
			INode obj;
			if (nodes.get(abbr_label_subj.toString()) == null) {
				sub = graph.createNode();
				nodes.put(abbr_label_subj, sub);

				graph.addLabel(sub, ExteriorLabelModel.SOUTH, abbr_label_subj);

			} else {
				sub = nodes.get(abbr_label_subj);
			}

			if (nodes.get(abbr_label_obj.toString()) == null) {
				obj = graph.createNode();
				nodes.put(abbr_label_obj, obj);
				graph.addLabel(obj, ExteriorLabelModel.SOUTH, abbr_label_obj);

			} else {

				obj = nodes.get(abbr_label_obj);
			}

			/**
			 * DOES NOT SUPPORT MULTIGRAPHS!!! Concatenate the edge labels
			 */

			IEdge temp_edge;
			String edge_label = "";
			if (graph.getEdge(sub, obj) != null) {
				temp_edge = graph.getEdge(sub, obj);
				graph.remove(temp_edge);
				edge_label = temp_edge.getLabels().getItem(0).getText();
				if (!edge_label.contains(abbr_label_pred))
					edge_label = edge_label + " | " + abbr_label_pred;
				temp_edge = graph.createEdge(sub, obj);

			} else {
				temp_edge = graph.createEdge(sub, obj);
				edge_label = abbr_label_pred;
			}

			RotatedSliderEdgeLabelModel rselm = new RotatedSliderEdgeLabelModel();
			rselm.setDistance(5);

			graph.addLabel(temp_edge, rselm.createParameterFromSource(0, 0.5),
					edge_label);
			if (!all_edges.contains(edge_label))
				all_edges.add(edge_label);

		}

		listPredicatesInExpl.setItems(all_edges);

		graphQueryingControl.fitGraphBounds();

		/*
		 * 
		 * 
		 * 
		 */

		queryingTab.setDisable(false);

		String pattern = recap
				.getExplanationBuilder()
				.getEntitySuggestion()
				.getPrototypicalPattern(current_explanation, source_entity,
						target_entity);

		String query = "SELECT DISTINCT ?vsource ?vtarget WHERE { " + pattern
				+ " FILTER( ?vsource != ?vtarget) }";

		// Model to_be_print =
		// recap.getExplanationBuilder().getEntitySuggestion()
		// .getPatternAsModel(pattern);

		paintQueryGraph(current_explanation);

		txtAreaSPARQLQuery.setText(query);
	}

	// / END INNER CLASS

	/**
	 * Paint the query graph in the Querying TAB
	 * 
	 * @param model
	 */

	private void paintQueryGraph(Model model)

	{
		// set up input modes
		final GraphEditorInputMode graphViewerInputMode = new GraphEditorInputMode();

		graphViewerInputMode.setToolTipItems(GraphItemTypes.LABELED_ITEM);

		graphViewerInputMode.setSelectableItems(GraphItemTypes.NODE);
		graphViewerInputMode.setFocusableItems(GraphItemTypes.NODE);
		graphViewerInputMode.setClickableItems(GraphItemTypes.NODE);
		graphViewerInputMode.setMovableItems(GraphItemTypes.NODE);

		/*
		 * graphViewerInputMode.setSelectableItems(FlagsEnum.or(
		 * GraphItemTypes.NODE, GraphItemTypes.EDGE));
		 * graphViewerInputMode.setClickableItems(FlagsEnum.or(
		 * 
		 * GraphItemTypes.NODE, GraphItemTypes.EDGE));
		 * 
		 * graphViewerInputMode.setFocusableItems(FlagsEnum.or(
		 * GraphItemTypes.NODE, GraphItemTypes.EDGE));
		 * graphViewerInputMode.setMovableItems
		 * (FlagsEnum.or(GraphItemTypes.NODE, GraphItemTypes.EDGE));
		 */

		graphViewerInputMode.setSnapContext(new GraphSnapContext());

		// do not allow to create new nodes/edges //ENABLE to modify the query!
		graphViewerInputMode.getCreateEdgeInputMode().setEnabled(false);
		graphViewerInputMode.setNodeCreationAllowed(false);

		// handles the movement of nodes/edges
		graphViewerInputMode.getMoveInputMode().setPositionHandler(
				new MyPositionHandler(graphViewerInputMode.getMoveInputMode()
						.getPositionHandler()));

		/**
		 * STYLE
		 */
		setQueryGraphNodeEdgeStyle();

		IGraph graph = getQueryGraph();

		graph.clear();

		Hashtable<String, INode> nodes = new Hashtable<String, INode>();
		StmtIterator st = model.listStatements();
		while (st.hasNext()) {
			Statement stmt = st.nextStatement(); // get next statement
			Resource subject = stmt.getSubject(); // get the subject
			Property predicate = stmt.getPredicate(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object
			String sub_string = subject.toString();
			String abbr_label_subj = getAbbreviatedLabel(sub_string);

			String pred_string = predicate.toString();

			String abbr_label_pred = getAbbreviatedLabel(pred_string);
			String obj_string = object.toString();

			String abbr_label_obj = getAbbreviatedLabel(obj_string);
			INode sub;
			INode obj;
			if (nodes.get(abbr_label_subj.toString()) == null) {
				sub = graph.createNode();

				nodes.put(abbr_label_subj, sub);

				graph.addLabel(sub, ExteriorLabelModel.NORTH, abbr_label_subj);

			} else {
				sub = nodes.get(abbr_label_subj);
			}

			if (nodes.get(abbr_label_obj.toString()) == null) {
				obj = graph.createNode();
				nodes.put(abbr_label_obj, obj);
				graph.addLabel(obj, ExteriorLabelModel.SOUTH, abbr_label_obj);

			} else {

				obj = nodes.get(abbr_label_obj);
			}

			/**
			 * DOES NOT SUPPORT MULTIGRAPHS!!! Concatenate the edge labels
			 */

			IEdge temp_edge;
			String edge_label = "";
			if (graph.getEdge(sub, obj) != null) {
				temp_edge = graph.getEdge(sub, obj);
				graph.remove(temp_edge);
				edge_label = temp_edge.getLabels().getItem(0).getText();
				if (!edge_label.contains(abbr_label_pred))
					edge_label = edge_label + " | " + abbr_label_pred;
				temp_edge = graph.createEdge(sub, obj);

			} else {
				temp_edge = graph.createEdge(sub, obj);
				edge_label = abbr_label_pred;
			}

			RotatedSliderEdgeLabelModel rselm = new RotatedSliderEdgeLabelModel();
			rselm.setDistance(5);

			graph.addLabel(temp_edge, rselm.createParameterFromSource(0, 0.5),
					edge_label);

			/**
			 * END STYLE
			 */

			//

			LayoutGraphAdapter adapter = new LayoutGraphAdapter(
					graphQueryingControl.getGraph());

			// copiedLayoutGraphQuerying = new CopiedLayoutGraph(adapter,
			// adapter);

			// layouterGraphQuery = startLayouterGraphQuery();

			// wakeUpGraphQuery();

			graphQueryingControl.currentItemProperty().addListener(
					new ChangeListener<IModelItem>() {
						@Override
						public void changed(
								ObservableValue<? extends IModelItem> observableValue,
								IModelItem iModelItem, IModelItem iModelItem2) {
							onCurrentItemChanged();

						}
					});

			// Set the input mode
			graphQueryingControl.setInputMode(graphViewerInputMode);

			updateViewportQueryPanelGraph();

		}

	}

	/**
	 * Sets up default styles for graph elements.
	 * <p>
	 * Default styles apply only to elements created after the default style has
	 * been set, so typically, you'd set these as early as possible in your
	 * application.
	 * </p>
	 */
	private void setExplanationGraphNodeEdgeStyle() {

		IGraph graph = getExplanationGraph();

		ImageNodeStyle ins = new ImageNodeStyle();
		ins.setUrl("/eu/gpirro/recap/controllers/usericon_male1.png");
		graph.getNodeDefaults().setStyle(ins);
		SizeD imageSize = new SizeD(36, 36);
		graph.getNodeDefaults().setSize(imageSize);

		// EDGES

		Pen defaultPen = new Pen(Color.GRAY, 2);
		PolylineEdgeStyle defaultEdgeStyle = new PolylineEdgeStyle(defaultPen);
		defaultEdgeStyle.setSourceArrow(DefaultArrow.CIRCLE);
		defaultEdgeStyle.setTargetArrow(DefaultArrow.TRIANGLE);
		graph.getEdgeDefaults().setStyle(defaultEdgeStyle);

		// LABELS
		ILabelStyle nodeLabelStyle = new SimpleLabelStyle(new TextFormat(
				new Font("Tahoma", 14)), Color.BLACK);

		ILabelStyle edgeLabelStyle = new SimpleLabelStyle(new TextFormat(
				new Font("Tahoma", 8)), Color.GREEN);

		// And set the style as default for both edge and node labels:

		graph.getEdgeDefaults().getLabels().setStyle(edgeLabelStyle);
		graph.getNodeDefaults().getLabels().setStyle(nodeLabelStyle);

	}

	/**
	 * Use this explanation to get a pattern for querying
	 * 
	 * @param event
	 */
	@FXML
	void queryingKGButtonHandler(ActionEvent event) {

		// queryingTab.setDisable(false);

		String pattern = recap
				.getExplanationBuilder()
				.getEntitySuggestion()
				.getPrototypicalPattern(current_explanation, source_entity,
						target_entity);

		String query = "SELECT DISTINCT ?vsource ?vtarget WHERE { " + pattern
				+ " FILTER( ?vsource != ?vtarget) }";

		// recap.getExplanationBuilder().getEntitySuggestion() //
		// .getPatternAsModel(pattern);

		paintQueryGraph(current_explanation);

		tabPaneGeneral.getSelectionModel().select(2);

		txtAreaSPARQLQuery.setText(query);

	}

	/**
	 * Style of edges and nodes for the query graph
	 */
	private void setQueryGraphNodeEdgeStyle() {

		IGraph graph = getQueryGraph();

		ImageNodeStyle ins = new ImageNodeStyle();
		ins.setUrl("/eu/gpirro/recap/controllers/usericon_male1.png");
		graph.getNodeDefaults().setStyle(ins);
		SizeD imageSize = new SizeD(20, 20);
		graph.getNodeDefaults().setSize(imageSize);

		// EDGES

		Pen defaultPen = new Pen(Color.GRAY, 2);
		PolylineEdgeStyle defaultEdgeStyle = new PolylineEdgeStyle(defaultPen);
		defaultEdgeStyle.setSourceArrow(DefaultArrow.CIRCLE);
		defaultEdgeStyle.setTargetArrow(DefaultArrow.TRIANGLE);
		graph.getEdgeDefaults().setStyle(defaultEdgeStyle);

		// LABELS
		ILabelStyle nodeLabelStyle = new SimpleLabelStyle(new TextFormat(
				new Font("Tahoma", 9)), Color.BLACK);

		ILabelStyle edgeLabelStyle = new SimpleLabelStyle(new TextFormat(
				new Font("Tahoma", 7)), Color.GREEN);

		// And set the style as default for both edge and node labels:

		graph.getEdgeDefaults().getLabels().setStyle(edgeLabelStyle);
		graph.getNodeDefaults().getLabels().setStyle(nodeLabelStyle);

	}

	//

	@FXML
	void sliderDiversityHandler(ActionEvent event) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {

					double div_value = Double.parseDouble(value_diversity
							.getText());

					Vector res = recap.getExplanationBuilder().DIVERSITY(
							source_entity, target_entity, div_value);

					Model div_expl = (Model) res.get(0);

					Hashtable<String, String> stats = new Hashtable<String, String>();

					stats.put(Constants.TIME_DIVERSITY, res.get(1) + "");
					stats.put(Constants.SIZE_DIVERSITY, div_expl.size() + "");
					stats.put(Constants.RES_TOTAL_NUM_PATHS, recap.getPaths()
							.size() + "");

					writeStatisticsOnTable(stats, Constants.DIVERSITY);

					paintExplanation(div_expl);

					labTypeExplan.setText("Diversity-based Explanation ("
							+ div_value + "%)");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * Computes the top-k paths
	 * 
	 * @param event
	 */
	@FXML
	void sliderPathsHandler(ActionEvent event) {

		Platform.runLater(new Runnable() {
			@Override
			public void run()

			{
				try {

					int K = Integer.parseInt(value_top_k_paths.getText());

					Vector res = recap.getExplanationBuilder()
							.TOPK_PATH_INSTANCES_LOCAL(source_entity,
									target_entity, K);

					Model model = (Model) res.get(0);
					Hashtable<String, String> stats = new Hashtable<String, String>();

					stats.put(Constants.TIME_TOPK, res.get(1) + "");

					stats.put(Constants.SIZE_TOPK, model.size() + "");
					stats.put(Constants.RES_TOTAL_NUM_PATHS, recap.getPaths()
							.size() + "");

					paintExplanation(model);

					labTypeExplan.setText("Top-" + K + " Paths Explanation");

					writeStatisticsOnTable(stats, Constants.TOPK);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	@FXML
	void sliderPatternHandler(ActionEvent event) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {

					/**
					 * The number of patterns is lower that the number of paths!
					 * 
					 */

					Vector res = recap.getExplanationBuilder()
							.TOPK_PATH_PATTERNS_LOCAL(
									Integer.parseInt(value_top_k_patterns
											.getText()));

					Model model_topk_patterns = (Model) res.get(0);

					Hashtable<String, String> stats = new Hashtable<String, String>();

					stats.put(Constants.TIME_TOPK_PATTERNS, res.get(1) + "");
					stats.put(Constants.SIZE_TOPK_PATTERNS,
							model_topk_patterns.size() + "");
					stats.put(Constants.RES_TOTAL_NUM_PATHS, recap.getPaths()
							.size() + "");

					writeStatisticsOnTable(stats, Constants.TOPK_PATTERNS);

					labTypeExplan.setText("Top-"
							+ value_top_k_patterns.getText()
							+ " Patterns Explanation");

					paintExplanation(model_topk_patterns);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	@FXML
	private void sourceTextFieldChangeHandler(ActionEvent e) {
		System.out.println("CHANGE=" + e.toString());

		/**
		 * AUTOCOMPLETE
		 * 
		 * @FXML
		 * 
		 */
		AutoCompletionBinding<String> autoCompletionBinding;
		String[] _possibleSuggestions = { "Hey", "Hello", "Hello World",
				"Apple", "Cool", "Costa", "Cola", "Coca Cola" };
		Set<String> possibleSuggestions = new HashSet<>(
				Arrays.asList(_possibleSuggestions));

		TextFields.bindAutoCompletion(targetEntityTextField, "Hey", "Hello",
				"Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola");

	}

	/**
	 * Handles the layout
	 * 
	 * @return
	 */
	private InteractiveOrganicLayouter startLayouter() {
		// create the layouter
		final InteractiveOrganicLayouter organicLayouter = new InteractiveOrganicLayouter();
		organicLayouter.setMaxTime(2000);

		organicLayouter.setPreferredNodeDistance(200);

		organicLayouter.setWorkingRatio(0.8);

		organicLayouter.setQuality(1.0);

		// use an animator that animates an infinite animation. This means that
		// all
		// changes to the layout are animated, as long as the demo is running.
		final Animator animator = new Animator(graphExplanationControl);
		animator.setAutoInvalidationEnabled(false);
		animator.setUsingWaitInputMode(false);
		animator.animate(new AnimationHandler() {
			private boolean hasLayouterStarted;

			@Override
			public void animate(double time) {
				// wait until the layouter has been started
				if (!hasLayouterStarted) {
					if (organicLayouter.isRunning()) {
						hasLayouterStarted = true;
					}
					return;
				}

				// now the layouter has been started and we check if it is still
				// running.
				// If not we destroy the animator otherwise we apply the layout
				// to the graph
				if (!organicLayouter.isRunning()) {
					animator.destroy();
					return;
				}
				if (organicLayouter.commitPositionsSmoothly(50, 0.05) > 0) {
					graphExplanationControl.update();
				}
			}
		}, Duration.INDEFINITE);

		// run the layouter in a separate thread. If we want a recalculation of
		// the layout,
		// we need to call the wakeup method of the InteractiveOrganicLayouter
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				organicLayouter.doLayout(copiedLayoutGraph);
				// stop the animator when the layout returns (does not normally
				// happen at all)
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						animator.destroy();
					}
				});
			}
		});
		thread.setDaemon(true);
		thread.start();
		return organicLayouter;
	}

	/**
	 * Handles the layout
	 * 
	 * @return
	 */
	private InteractiveOrganicLayouter startLayouterGraphQuery() {
		// create the layouter
		final InteractiveOrganicLayouter organicLayouter = new InteractiveOrganicLayouter();
		organicLayouter.setMaxTime(2000);

		organicLayouter.setPreferredNodeDistance(200);

		organicLayouter.setWorkingRatio(0.8);

		organicLayouter.setQuality(1.0);

		// use an animator that animates an infinite animation. This means that
		// all
		// changes to the layout are animated, as long as the demo is running.
		final Animator animator = new Animator(graphQueryingControl);
		animator.setAutoInvalidationEnabled(false);
		animator.setUsingWaitInputMode(false);
		animator.animate(new AnimationHandler() {
			private boolean hasLayouterStarted;

			@Override
			public void animate(double time) {
				// wait until the layouter has been started
				if (!hasLayouterStarted) {
					if (organicLayouter.isRunning()) {
						hasLayouterStarted = true;
					}
					return;
				}

				// now the layouter has been started and we check if it is still
				// running.
				// If not we destroy the animator otherwise we apply the layout
				// to the graph
				if (!organicLayouter.isRunning()) {
					animator.destroy();
					return;
				}
				if (organicLayouter.commitPositionsSmoothly(50, 0.05) > 0) {
					graphQueryingControl.update();
				}
			}
		}, Duration.INDEFINITE);

		// run the layouter in a separate thread. If we want a recalculation of
		// the layout,
		// we need to call the wakeup method of the InteractiveOrganicLayouter
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				organicLayouter.doLayout(copiedLayoutGraphQuerying);
				// stop the animator when the layout returns (does not normally
				// happen at all)
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						animator.destroy();
					}
				});
			}
		});
		thread.setDaemon(true);
		thread.start();
		return organicLayouter;
	}

	//

	/**
	 * Update the progress bar that indicates the progress of the query
	 * execution
	 * 
	 * @param current_value
	 */
	public void updateProgressBar(String current_value) {

		progressBarInitial.setProgress(Double.parseDouble(current_value));
		if (current_value.startsWith("1.")) {

			secondTabMain.setDisable(false);
		}
	}

	/**
	 * Updates the content rectangle to encompass all existing graph elements
	 */
	void updateViewportExplanation() {

		// tabpaneGeneral.autosize();

		graphExplanationControl.updateContentRect();
		graphExplanationControl.fitContent();
		// The sequence above is equivalent to just calling:
		graphExplanationControl.fitGraphBounds();
		LayoutExtensions.doLayout(new IncrementalHierarchicLayouter(),
				graphExplanationControl.getGraph());
		// graphControl.updateContentRect();
	}

	/**
	 * Updates the content rectangle to encompass all existing graph elements
	 */
	void updateViewportQueryPanelGraph() {

		// tabpaneGeneral.autosize();

		graphQueryingControl.updateContentRect();
		graphQueryingControl.fitContent();
		// The sequence above is equivalent to just calling:
		graphQueryingControl.fitGraphBounds();

		// ShuffleLayouter BUONO
		LayoutExtensions.doLayout(new ShuffleLayouter(),
				graphQueryingControl.getGraph());
	}

	/**
	 * Wakes the layouter up to calculate an initial layout.
	 */
	private void wakeUp() {
		if (layouter != null) {
			// we make all nodes freely movable
			for (Node copiedNode : copiedLayoutGraph.getNodes()) {
				layouter.setInertia(copiedNode, 0);
			}
			// then wake up the layouter
			layouter.wakeUp();

			// and after two second we freeze the nodes again...
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					for (Node copiedNode : copiedLayoutGraph.getNodes()) {
						layouter.setInertia(copiedNode, 1);
					}
					timer.cancel();
				}
			}, 2000);
		}
	}

	/**
	 * Wakes the layouter up to calculate an initial layout.
	 */
	private void wakeUpGraphQuery() {
		if (layouterGraphQuery != null) {
			// we make all nodes freely movable
			for (Node copiedNode : copiedLayoutGraphQuerying.getNodes()) {
				layouterGraphQuery.setInertia(copiedNode, 0);
			}
			// then wake up the layouter
			layouterGraphQuery.wakeUp();

			// and after two second we freeze the nodes again...
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					for (Node copiedNode : copiedLayoutGraphQuerying.getNodes()) {
						layouterGraphQuery.setInertia(copiedNode, 1);
					}
					timer.cancel();
				}
			}, 2000);
		}
	}

	/**
	 * Populate the list of path instances
	 * 
	 * @param instances
	 */
	void writePathIstances(ArrayList<Model> instances) {
		ObservableList<String> all_instances = FXCollections
				.observableArrayList();

		String model_s;
		for (Model inst : instances) {

			model_s = recap.getExplanationBuilder().convertModelToString(inst);
			if (!all_instances.contains(model_s)) {
				all_instances.add(model_s);

				string_to_model_instances.put(model_s, inst);

			}

		}

		listPathInstances.setItems(all_instances);

		// maybe print all the path instances as well of the graph view!!!!!
	}

	/**
	 * Write statistics about path retrieval
	 */
	private void writeStatisticsOnTable(Hashtable<String, String> stats,
			String type_stats) {

		ObservableList<String> res = FXCollections.observableArrayList();

		if (type_stats.equalsIgnoreCase(Constants.PATH_STATS_RES)) {

			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time retrieving paths: "
					+ stats.get(Constants.RES_TOTAL_TIME_PATHS) + " ms");
			res.add("Number of paths: "
					+ stats.get(Constants.RES_TOTAL_NUM_PATHS));
			res.add("Number of queries with results: "
					+ stats.get(Constants.RES_TOTAL_NUM_QUERIES_WITH_RES_PATHS));
			res.add("Time Merge explanation: "
					+ stats.get(Constants.TIME_EXPL_MERGE) + " ms");

		}

		else if (type_stats.equalsIgnoreCase(Constants.MIP)) {

			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time Most Informative Paths: "
					+ stats.get(Constants.TIME_MIP) + " ms");
			res.add("Size Most Informative Paths: "
					+ stats.get(Constants.SIZE_MIP));
			res.add("Number of paths: "
					+ stats.get(Constants.RES_TOTAL_NUM_PATHS));
		}

		else if (type_stats.equalsIgnoreCase(Constants.TOPK)) {
			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time Top-" + value_top_k_paths.getText() + " Paths: "
					+ stats.get(Constants.TIME_TOPK) + " ms");
			res.add("Size Top-" + value_top_k_paths.getText() + " Paths: "
					+ stats.get(Constants.SIZE_TOPK));
			res.add("Number of paths: "
					+ stats.get(Constants.RES_TOTAL_NUM_PATHS));
		}

		else if (type_stats.equalsIgnoreCase(Constants.DIVERSITY)) {
			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time diversity: " + stats.get(Constants.TIME_DIVERSITY)
					+ " ms");
			res.add("Size diversity: " + stats.get(Constants.SIZE_DIVERSITY));
		} else if (type_stats.equalsIgnoreCase(Constants.TOPK_PATTERNS)) {
			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time Top-" + value_top_k_patterns.getText()
					+ " Patterns: " + stats.get(Constants.TOPK_PATTERNS)
					+ " ms");
			res.add("Size Top-" + value_top_k_patterns.getText()
					+ " Patterns: " + stats.get(Constants.SIZE_TOPK_PATTERNS));
			res.add("Number of paths: "
					+ stats.get(Constants.RES_TOTAL_NUM_PATHS));
		}

		else if (type_stats.equalsIgnoreCase(Constants.ALL_PATTERNS)) {
			res.add("SourceE: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(source_entity));
			res.add("Target: "
					+ eu.gpirro.utilities.Util
							.getObjectShortName(target_entity));
			res.add("Distance (up to): " + distance);

			res.add("Time Patterns: "
					+ stats.get(Constants.TIME_EXPL_MERGE_PATTERNS) + " ms");
			res.add("Total number of patterns: "
					+ stats.get(Constants.TOTAL_NUM_EXPL_PATTERNS));
			res.add("Number of paths: "
					+ stats.get(Constants.RES_TOTAL_NUM_PATHS));
		}

		listResultsPaths.setItems(res);

	}

}
