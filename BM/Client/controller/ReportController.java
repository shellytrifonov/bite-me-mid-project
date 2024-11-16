package controller;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.DatePicker;
import client.ClientController;
import entities.Message;
import entities.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.Node;
import javafx.util.StringConverter;

/**
 * Controller class for managing report generation and display in the Bite Me application.
 * This class handles user interactions for various types of reports including income,
 * orders, performance, and quarterly reports.
 * @author Yuval Shahar
 */
public class ReportController implements Initializable  {
    
	/**
	 * Button to generate an income report.
	 */
	@FXML private Button incomeReportButton;

	/**
	 * Button to generate an orders report.
	 */
	@FXML private Button ordersReportButton;

	/**
	 * Button to generate a performance report.
	 */
	@FXML private Button performanceReportButton;

	/**
	 * Button to generate a quarterly report. Only visible to users with CEO role.
	 */
	@FXML private Button quarterlyReportButton;

	/**
	 * Button to navigate back to the previous screen.
	 */
	@FXML private Button backButton;

	/**
	 * Controller for handling client-side operations and communication with the server.
	 */
	private ClientController clientController;

	/**
	 * The currently logged-in user.
	 */
	private User currentUser;

	/**
	 * The Stage for the manager's screen, used for navigation.
	 */
	private Stage managerStage;

	/**
	 * The current Stage for this report controller.
	 */
	private Stage stage;

	/**
	 * List of currently open report stages. Used to manage multiple open report windows.
	 */
	private List<Stage> openReportStages = new ArrayList<>();

	  /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
	public void initialize(URL location, ResourceBundle resources) {
    	Locale.setDefault(new Locale("en", "US"));
    }
    
    /**
     * Sets the current user for the ReportController and updates button visibility.
     * This method determines the visibility of specific buttons based on the user's role.
     *
     * @param currentUser The current user to be set.
     */
    public void setUser(User currentUser) {
        this.currentUser = currentUser;
        updateButtonVisibility();
    }
    
    /**
     * Sets the ClientController for this ReportController.
     * This controller is used for server communication.
     *
     * @param clientController The ClientController to be used.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }
    
    /**
     * Sets the manager stage for this ReportController.
     * This stage is used to return to the manager view when closing the report view.
     *
     * @param managerStage The Stage in which manager controller's UI is displayed.
     */
    public void setManagerStage(Stage managerStage) {
        this.managerStage = managerStage;
    }
    
    /**
     * Sets the current stage for this ReportController.
     * This stage represents the current report view.
     *
     * @param stage The Stage in which this controller's UI is displayed.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Updates the visibility of report buttons based on the current user's role.
     * The quarterly report button is only visible to users with CEO role.
     */
    private void updateButtonVisibility() {
        boolean isCEO = currentUser.getRole() == User.UserRole.CEO;
        quarterlyReportButton.setVisible(isCEO);
        quarterlyReportButton.setManaged(isCEO);
    }

    /**
     * Handles the action when the Income Report button is clicked.
     * This method determines whether to generate a CEO or Branch Manager income report based on the user's role.
     */
    @FXML
    private void handleIncomeReport() {
        if (currentUser.getRole() == User.UserRole.CEO) {
            handleCEOIncomeReport();
        } else {
            handleBranchManagerIncomeReport();
        }
    }
    
    /**
     * Handles the action when the Orders Report button is clicked.
     * This method determines whether to generate a CEO or Branch Manager orders report based on the user's role.
     */
    @FXML
    private void handleOrdersReport() {
        if (currentUser.getRole() == User.UserRole.CEO) {
            handleCEOOrdersReport();
        } else {
            handleBranchManagerOrdersReport();
        }
    }
    
    /**
     * Handles the action when the Performance Report button is clicked.
     * This method determines whether to generate a CEO or Branch Manager performance report based on the user's role.
     */
    @FXML
    private void handlePerformanceReport() {
        if (currentUser.getRole() == User.UserRole.CEO) {
            handleCEOPerformanceReport();
        } else {
            handleBranchManagerPerformanceReport();
        }
    }
    
    /**
     * Handles the generation of an income report for CEO users.
     * Opens a dialog for selecting branch and date range, then generates the report.
     */
    private void handleCEOIncomeReport() {
        Dialog<Pair<Pair<LocalDate, LocalDate>, String>> dialog = new Dialog<>();
        dialog.setTitle("Choose branch and date range");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        ComboBox<String> branchSelector = new ComboBox<>();
        branchSelector.getItems().addAll("north", "center", "south");

        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Branch:"), branchSelector,
            new Label("Start date:"), startDatePicker,
            new Label("End date:"), endDatePicker));

        startDatePicker.setConverter(getDateConverter());
        endDatePicker.setConverter(getDateConverter());
        
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
      
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String selectedBranch = branchSelector.getValue();
                
                if (startDate != null && endDate != null && selectedBranch != null && isValidDateRange(startDate, endDate)) {
                    return new Pair<>(new Pair<>(startDate, endDate), selectedBranch);
                } else {
                    showAlert("Invalid Input", "Please ensure all fields are filled and the date range is valid.");
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<Pair<LocalDate, LocalDate>, String>> result = dialog.showAndWait();

        result.ifPresent(dateRangeAndBranch -> {
            Pair<LocalDate, LocalDate> dateRange = dateRangeAndBranch.getKey();
            String branchId = dateRangeAndBranch.getValue();
            generateIncomeReport(dateRange.getKey(), dateRange.getValue(), branchId);
        });
    }
    
    /**
     * Handles the generation of an income report for CEO users.
     * Opens a dialog for selecting branch and date range, then generates the report.
     */
    private void handleCEOOrdersReport() {
        Dialog<Pair<Pair<LocalDate, LocalDate>, String>> dialog = new Dialog<>();
        dialog.setTitle("Choose branch and date range for Orders Report");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        ComboBox<String> branchSelector = new ComboBox<>();
        branchSelector.getItems().addAll("north", "center", "south");

        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Branch:"), branchSelector,
            new Label("Start date:"), startDatePicker,
            new Label("End date:"), endDatePicker));

        startDatePicker.setConverter(getDateConverter());
        endDatePicker.setConverter(getDateConverter());
        
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
      
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String selectedBranch = branchSelector.getValue();
                
                if (startDate != null && endDate != null && selectedBranch != null && isValidDateRange(startDate, endDate)) {
                    return new Pair<>(new Pair<>(startDate, endDate), selectedBranch);
                } else {
                    showAlert("Invalid Input", "Please ensure all fields are filled and the date range is valid.");
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<Pair<LocalDate, LocalDate>, String>> result = dialog.showAndWait();

        result.ifPresent(dateRangeAndBranch -> {
            Pair<LocalDate, LocalDate> dateRange = dateRangeAndBranch.getKey();
            String branchId = dateRangeAndBranch.getValue();
            generateOrdersReport(dateRange.getKey(), dateRange.getValue(), branchId);
        });
    }

    /**
     * Handles the generation of a performance report for CEO users.
     * Opens a dialog for selecting branch and date range, then generates the report.
     */
    private void handleCEOPerformanceReport() {
        Dialog<Pair<Pair<LocalDate, LocalDate>, String>> dialog = new Dialog<>();
        dialog.setTitle("Choose branch and date range for Performance Report");

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        ComboBox<String> branchSelector = new ComboBox<>();
        branchSelector.getItems().addAll("north", "center", "south");

        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Branch:"), branchSelector,
            new Label("Start date:"), startDatePicker,
            new Label("End date:"), endDatePicker));

        startDatePicker.setConverter(getDateConverter());
        endDatePicker.setConverter(getDateConverter());
        
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
      
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String selectedBranch = branchSelector.getValue();
                
                if (startDate != null && endDate != null && selectedBranch != null && isValidDateRange(startDate, endDate)) {
                    return new Pair<>(new Pair<>(startDate, endDate), selectedBranch);
                } else {
                    showAlert("Invalid Input", "Please ensure all fields are filled and the date range is valid.");
                    return null;
                }
            }
            return null;
        });
        Optional<Pair<Pair<LocalDate, LocalDate>, String>> result = dialog.showAndWait();

        result.ifPresent(dateRangeAndBranch -> {
            Pair<LocalDate, LocalDate> dateRange = dateRangeAndBranch.getKey();
            String branchId = dateRangeAndBranch.getValue();
            generatePerformanceReport(dateRange.getKey(), dateRange.getValue(), branchId);
        }); 
    }
    
    /**
     * Handles the generation of an income report for Branch Manager users.
     * Opens a dialog for selecting date range, then generates the report for the manager's branch.
     */
    private void handleBranchManagerIncomeReport() {
    	  Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
    	    dialog.setTitle("Choose range dates");

    	    DatePicker startDatePicker = new DatePicker();
    	    DatePicker endDatePicker = new DatePicker();    

    	    dialog.getDialogPane().setContent(new VBox(10, 
    	        new Label("Start date:"), startDatePicker,
    	        new Label("End date:"), endDatePicker));

    	    startDatePicker.setConverter(getDateConverter());
    	    endDatePicker.setConverter(getDateConverter());
    	    
    	    // Add a listener to validate the end date when either date changes
    	    startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
    	    endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
    	  
    	    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    	    dialog.setResultConverter(dialogButton -> {
    	        if (dialogButton == ButtonType.OK) {
    	        	 LocalDate startDate = startDatePicker.getValue();
   	                 LocalDate endDate = endDatePicker.getValue();
   	              
   	              if (startDate != null && endDate != null && isValidDateRange(startDate, endDate)) {
   	            	  return new Pair<>(startDate, endDate);
   	              } else {
   	               // Show an error message if the date range is invalid
   	               showAlert("Invalid Date Range", "Please ensure the end date is in the same month as the start date and not before the start date.");
   	               return null;
   	           }
    	        }
    	        return null;
    	    });

    	    Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();
    	    System.out.println(currentUser.getUserId());
    	    result.ifPresent(dateRange -> {
    	        generateIncomeReport(dateRange.getKey(), dateRange.getValue(),currentUser.getUserId());
    	    });    
     }

    /**
     * Handles the generation of an orders report for Branch Manager users.
     * Opens a dialog for selecting date range, then generates the report for the manager's branch.
     */
    public void handleBranchManagerOrdersReport() {
    	Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
	    dialog.setTitle("Choose range dates");

	    DatePicker startDatePicker = new DatePicker();
	    DatePicker endDatePicker = new DatePicker();    

	    dialog.getDialogPane().setContent(new VBox(10, 
	        new Label("Start date:"), startDatePicker,
	        new Label("End date:"), endDatePicker));

	    startDatePicker.setConverter(getDateConverter());
	    endDatePicker.setConverter(getDateConverter());
	    // Add a listener to validate the end date when either date changes
	    startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
	    endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDates(startDatePicker, endDatePicker));
	  
	    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == ButtonType.OK) {
	        	 LocalDate startDate = startDatePicker.getValue();
	              LocalDate endDate = endDatePicker.getValue();
	              if (startDate != null && endDate != null && isValidDateRange(startDate, endDate)) {
	            	  return new Pair<>(startDate, endDate);
	   	              } else {
	   	               // Show an error message if the date range is invalid
	   	               showAlert("Invalid Date Range", "Please ensure the end date is in the same month as the start date and not before the start date.");
	   	               return null;
	   	           }
	    	        }
	    	        return null;
	    	    });

	    	    Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();

	    	    result.ifPresent(dateRange -> {
	    	    	generateOrdersReport(dateRange.getKey(), dateRange.getValue(),currentUser.getUserId());
	    	    });    
	     }
	        
    /**
     * Handles the generation of a performance report for Branch Manager users.
     * Opens a dialog for selecting date range, then generates the report for the manager's branch.
     */
    public void handleBranchManagerPerformanceReport() {
    	Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
	    dialog.setTitle("Choose range dates");

	    DatePicker startDatePicker = new DatePicker();
	    DatePicker endDatePicker = new DatePicker();    

	    dialog.getDialogPane().setContent(new VBox(10, 
	        new Label("Start date:"), startDatePicker,
	        new Label("End date:"), endDatePicker));

	    startDatePicker.setConverter(getDateConverter());
	    endDatePicker.setConverter(getDateConverter());
	    

	  
	    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == ButtonType.OK) {
	        	  LocalDate startDate = startDatePicker.getValue();
	              LocalDate endDate = endDatePicker.getValue();
	              
	              if (startDate != null && endDate != null && isValidDateRange(startDate, endDate)) {
	            	  return new Pair<>(startDate, endDate);
	   	              } else {
	   	               // Show an error message if the date range is invalid
	   	               showAlert("Invalid Date Range", "Please ensure the end date is in the same month as the start date and not before the start date.");
	   	               return null;
	   	           }
	    	        }
	    	        return null;
	    	    });

	    	    Optional<Pair<LocalDate, LocalDate>> result = dialog.showAndWait();

	    	    result.ifPresent(dateRange -> {
	    	    	generatePerformanceReport(dateRange.getKey(), dateRange.getValue(),currentUser.getUserId());
	    	    });    
	     }

    /**
     * Handles the action when the Quarterly Report button is clicked.
     * This method is typically only available to users with CEO role.
     */
    @FXML
    public void handleQuarterlyReport() {
        if (openReportStages.size() >= 2) {
            showAlert("Report limitation", "You can view at most two reports simultaneously. Close an existing report to open a new one..");
            return;
        }

        Dialog<Pair<Pair<Integer, Integer>, String>> dialog = new Dialog<>();
        dialog.setTitle("Choose quarter,year and branch");

        ComboBox<Integer> quarterSelector = new ComboBox<>();
        quarterSelector.getItems().addAll(1, 2, 3, 4);
        ComboBox<Integer> yearSelector = new ComboBox<>();
        yearSelector.getItems().addAll(LocalDate.now().getYear(), LocalDate.now().getYear() - 1);
        ComboBox<String> branchSelector = new ComboBox<>();
        branchSelector.getItems().addAll("North", "Center", "South");

        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Quarter:"), quarterSelector,
            new Label("Year:"), yearSelector,
            new Label("Branch:"), branchSelector));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(new Pair<>(quarterSelector.getValue(), yearSelector.getValue()), branchSelector.getValue());
            }
            return null;
        });

        Optional<Pair<Pair<Integer, Integer>, String>> result = dialog.showAndWait();

        result.ifPresent(quarterYearBranch -> {
            generateQuarterlyReport(
                quarterYearBranch.getKey().getKey(),
                quarterYearBranch.getKey().getValue(),
                quarterYearBranch.getValue()
            );
        });
    }
    
    /*
    * Generates a quarterly report for the specified quarter, year, and branch.
    * This method sends a request to the server and processes the response.
    *
    * @param quarter The quarter for which the report is generated.
    * @param year The year for which the report is generated.
    * @param branch The branch for which the report is generated.
    */
    private void generateQuarterlyReport(int quarter, int year, String branch) {
        Message<Object[]> request = new Message<>("QuarterlyReport", new Object[]{quarter, year, branch});
        clientController.accept(request);
        Message<?> response = clientController.getResponse("QuarterlyReportResponse");

        if (response != null && response.getType() instanceof Map) {
            @SuppressWarnings("unchecked")
			Map<String, Object> reportData = (Map<String, Object>) response.getType();
            displayQuarterlyReport(reportData);
        } else {
            showAlert("Error", "An error occurred while creating the quarterly report");
        }
    }
    
    /**
     * Displays the quarterly report data in a new window.
     *
     * @param reportData A map containing the report data including branch information and revenue data.
     */
    private void displayQuarterlyReport(Map<String, Object> reportData) {
        Stage reportStage = new Stage();
        String branch = (String) reportData.get("branch");
        Integer quarter = (Integer) reportData.get("quarter");
        Integer year = (Integer) reportData.get("year");
        reportStage.setTitle("Quarter Report - " + branch + " Q" + quarter + " " + year);
       
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Node chartWithLegend = createBranchHistogram(reportData);
        
        root.getChildren().addAll(chartWithLegend);

        Scene scene = new Scene(root, 800, 600);
        reportStage.setScene(scene);

        manageReportWindows(reportStage);

        reportStage.show();
    }

    /**
     * Creates a histogram chart representing branch data for a quarterly report.
     *
     * @param reportData A map containing the report data, including branch information,
     *                   quarter, year, and revenue data.
     * @return A Node containing the histogram chart and a total revenue table.
     */
    private Node createBranchHistogram(Map<String, Object> reportData) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        
        StackedBarChart<String, Number> barChart = new StackedBarChart<>(xAxis, yAxis);
        barChart.setTitle("Quarter Report Branch " + reportData.get("branch") + 
                          ", Q" + reportData.get("quarter") + " " + reportData.get("year"));
        
        xAxis.setLabel("Daily Order Range");
        yAxis.setLabel("Number Of Days");

        @SuppressWarnings("unchecked")
		Map<String, Map<String, Integer>> branchData = (Map<String, Map<String, Integer>>) reportData.get("branchData");
        @SuppressWarnings("unchecked")
		Map<String, Map<String, Double>> revenueData = (Map<String, Map<String, Double>>) reportData.get("revenueData");
        
        if (branchData == null || branchData.isEmpty() || revenueData == null || revenueData.isEmpty()) {
           showAlert("No data available for the selected period","No Data");
        }
        
        /* This method iterates through the branchData map, creating a new series for each restaurant.
        * For each restaurant, it then iterates through its data ranges, adding each data point to the series.
        * Finally, each series is added to the barChart.
        * 
        *The outer loop iterates over restaurant names, while the inner loop iterates over data ranges for each restaurant.
        * 
        * @throws NullPointerException if branchData or barChart is null
        * @throws ClassCastException if the map values are not of the expected types
        */
        for (String restaurantName : branchData.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(restaurantName);

            Map<String, Integer> restaurantData = branchData.get(restaurantName);
            for (String range : restaurantData.keySet()) {
                series.getData().add(new XYChart.Data<>(range, restaurantData.get(range)));
            }

            barChart.getData().add(series);
        }
    
        /* Add value labels to the bars
        iterates over each series in the {@link BarChart} and each data point within those series. 
        * For each data point, it retrieves the associated node (which represents the bar), 
        * and then adds a {@link VBox} containing additional details such as the number of days and revenue.
        */
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                StackPane bar = (StackPane) data.getNode();
                String restaurantName = series.getName();
                String range = data.getXValue();
                int days = data.getYValue() != null ? data.getYValue().intValue() : 0;
                double revenue = 0.0;
                Map<String, Double> restaurantRevenue = revenueData.get(restaurantName);
                if (restaurantRevenue != null) {
                    Double revenueValue = restaurantRevenue.get(range);
                    if (revenueValue != null) {
                        revenue = revenueValue;
                    }
                }
               
                VBox vbox = new VBox(2);
                Label daysLabel = new Label(days + " Days");
                Label revenueLabel = new Label(String.format("₪%.0f", revenue));
                vbox.getChildren().addAll(daysLabel, revenueLabel);
                
                bar.getChildren().add(vbox);
            }
        }

        Pane chartPane = new Pane();
        chartPane.getChildren().add(barChart);

        for (String range : branchData.values().iterator().next().keySet()) {
            double totalRevenue = revenueData.values().stream()
                    .mapToDouble(map -> map.get(range))
                    .sum();
            
            Label totalLabel = new Label(String.format("₪%.0fK", totalRevenue / 1000));
            totalLabel.setRotate(-90);
            chartPane.getChildren().add(totalLabel);

            barChart.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
                Bounds b = barChart.getBoundsInParent();
                double xPos = barChart.getXAxis().getDisplayPosition(range) + b.getMinX() + barChart.getCategoryGap() / 2;
                double yPos = b.getMaxY();
                totalLabel.setLayoutX(xPos);
                totalLabel.setLayoutY(yPos);
            });
        }

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(chartPane, createTotalRevenueTable(revenueData));

        return vbox;
    }
    
    /**
     * Creates a table view displaying the total revenue for each restaurant and the branch.
     *
     * @param revenueData A map containing revenue data for each restaurant.
     * @return A Node containing the table view of total revenues.
     */
    @SuppressWarnings("unchecked")
	private Node createTotalRevenueTable(Map<String, Map<String, Double>> revenueData) {
        TableView<RestaurantRevenue> table = new TableView<>();
        
        TableColumn<RestaurantRevenue, String> nameCol = new TableColumn<>("Resturant");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<RestaurantRevenue, Double> revenueCol = new TableColumn<>("Total Income");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setCellFactory(this::createRevenueCell);
        
        table.getColumns().addAll(nameCol, revenueCol);
        
        for (Map.Entry<String, Map<String, Double>> entry : revenueData.entrySet()) {
            double totalRevenue = entry.getValue().values().stream().mapToDouble(Double::doubleValue).sum();
            table.getItems().add(new RestaurantRevenue(entry.getKey(), totalRevenue));
        }
        
        double totalBranchRevenue = table.getItems().stream()
                .mapToDouble(RestaurantRevenue::getRevenue)
                .sum();
        table.getItems().add(new RestaurantRevenue("Total income in the branch", totalBranchRevenue));
        
        return table;
    }
    
    /**
     * Creates a custom table cell for displaying revenue values in the specified format.
     *
     * @param column The TableColumn for which this cell factory is created.
     * @return A TableCell that formats revenue values as currency.
     */
    private TableCell<RestaurantRevenue, Double> createRevenueCell(TableColumn<RestaurantRevenue, Double> column) {
        return new TableCell<RestaurantRevenue, Double>() {
            @Override
            protected void updateItem(Double revenue, boolean empty) {
                super.updateItem(revenue, empty);
                if (empty || revenue == null) {
                    setText(null);
                } else {
                    setText(String.format("₪%.0f", revenue));
                }
            }
        };
    }
    
    /**
     * Manages the open report windows, ensuring that no more than two are open at a time.
     * If a third window is opened, the oldest window is closed.
     *
     * @param newStage The new Stage being opened for a report.
     */
    private void manageReportWindows(Stage newStage) {
        if (openReportStages.size() >= 2) {
            Stage oldestStage = openReportStages.remove(0);
            oldestStage.close();
        }
        openReportStages.add(newStage);
        
        newStage.setOnCloseRequest(event -> openReportStages.remove(newStage));
    }
    public static class RestaurantRevenue {
        private final String name;
        private final double revenue;

        public RestaurantRevenue(String name, double revenue) {
            this.name = name;
            this.revenue = revenue;
        }

        public String getName() { return name; }
        public double getRevenue() { return revenue; }
    }
    
    /*
    * Generates an income report for the specified date range.
    *
    * @param startDate The start date of the report period.
    * @param endDate The end date of the report period.
    */
    private void generateIncomeReport(LocalDate startDate, LocalDate endDate,String currentUser) {
        Message<Object[]> request = new Message<>("IncomeReport", new Object[]{startDate, endDate,currentUser});
        String region = getBranch(currentUser);
        clientController.accept(request);
        Message<?> response = clientController.getResponse("IncomeReportResponse");
        
        if (response != null && response.getType() instanceof Map) {
            @SuppressWarnings("unchecked")
			Map<String, Object> reportData = (Map<String, Object>) response.getType();
            displayIncomeReport(reportData, startDate, endDate,region);
        } else {
            showAlert("An error occurred in the income report","Error");

        }
    }
    
    /**
     * Converts a branch ID to a readable branch name.
     *
     * @param branchId The ID of the branch.
     * @return The readable name of the branch.
     */
    private String getBranch(String branchId) {
    	String loactionBranch;
    	switch (branchId) {
		case "north": {
			loactionBranch = "North branch";
			break;
		}
		case "center": {
			loactionBranch = "Center branch";
			break;
		}
		default:
			loactionBranch = "South branch";
			break;
			 
		}
		return loactionBranch;
    }
    
    /**
     * Generates an orders report for a specified date range and user.
     * This method sends a request to the server to generate the report, 
     * receives the response, and displays the report if the data is valid.
     *
     * @param startDate The start date of the report period (inclusive).
     * @param endDate The end date of the report period (inclusive).
     * @param currentUser The identifier of the current user requesting the report.
     *
     * @throws ClientException if there's an error in communication with the server.
     * @throws IllegalArgumentException if the response from the server is not in the expected format.
     *
     * Note: If the response is null or not of the expected type (Map), 
     * appropriate error handling should be implemented in the else block.
     */
    private void generateOrdersReport(LocalDate startDate, LocalDate endDate,String currentUser) {
        Message<Object[]> request = new Message<>("OrdersReport", new Object[]{startDate, endDate,currentUser});
        clientController.accept(request);
        Message<?> response = clientController.getResponse("OrderReportResponse");
        String region = getBranch(currentUser);
        if (response != null && response.getType() instanceof Map) {
            @SuppressWarnings("unchecked")
			Map<String, Object> reportData = (Map<String, Object>) response.getType();
            displayOrdersReport(reportData, startDate, endDate,region);
        } else {
            showAlert("An error occurred in the orders report","Error");

        }
    }
    
    /**
     * Generates a performance report for the specified date range and user.
     * This method sends a request to the server and processes the response.
     *
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @param currentUser The identifier of the current user requesting the report.
     */
    private void generatePerformanceReport(LocalDate startDate, LocalDate endDate,String currentUser) {
        Message<Object[]> request = new Message<>("PerformanceReport", new Object[]{startDate, endDate,currentUser});
        clientController.accept(request);
        Message<?> response = clientController.getResponse("PerformanceReportResponse");
        String region = getBranch(currentUser);

        if (response != null && response.getType() instanceof Map) {
            @SuppressWarnings("unchecked")
			Map<String, Object> reportData = (Map<String, Object>) response.getType();
            if (isReportDataEmpty(reportData)) {
                showAlert("No Data", "There is no performance data available for the selected period.");
            } else {
                displayGraphicalPerformanceReport(reportData, startDate, endDate, region);
            }
        } else {
            showAlert("Error", "An error occurred in the performance report");
        }
    }
    
    /**
     * Checks if the report data is empty or contains no significant information.
     *
     * @param reportData A map containing the report data.
     * @return true if the report data is considered empty, false otherwise.
     */
    private boolean isReportDataEmpty(Map<String, Object> reportData) {
        return reportData.get("totalDeliveries") == null || 
               ((Number) reportData.get("totalDeliveries")).intValue() == 0 ||
               reportData.get("performanceByDay") == null || 
               ((Map<?, ?>) reportData.get("performanceByDay")).isEmpty();
    }
    
    /**
     * Displays the orders report data in a formatted dialog.
     *
     * @param reportData A map containing the report data including total orders,
     *                   orders by type and top items by type.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     */
    @SuppressWarnings("unchecked")
	private void displayOrdersReport(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate,String region) {
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Orders Report of "+region+"\n");
        reportContent.append("==============\n\n");
        reportContent.append(String.format("Report period: %s - %s\n\n", startDate, endDate));

        int totalOrders = (int) reportData.get("totalOrders");
        reportContent.append(String.format("Total orders: %d\n\n", totalOrders));

        Map<String, Integer> ordersByType = (Map<String, Integer>) reportData.get("ordersByType");
        reportContent.append("Orders by item type:\n");
        for (Map.Entry<String, Integer> entry : ordersByType.entrySet()) {
            reportContent.append(String.format("%s: %d orders\n", entry.getKey(), entry.getValue()));
        }
        reportContent.append("\n");

        List<Map<String, Object>> topItems = (List<Map<String, Object>>) reportData.get("topItems");
        reportContent.append("Top 5 ordered items:\n");
        for (Map<String, Object> item : topItems) {
            reportContent.append(String.format("%s: %d orders\n", 
                item.get("itemName"), (int) item.get("orderCount")));
        }
        reportContent.append("\n");
        showReportDialog("Orders Report", reportContent.toString());
        }

    /**
     * Displays the orders report data in a formatted dialog.
     *
     * @param reportData A map containing the report data including total orders,
     *                   orders by type, top items, and revenue by type.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     */
    private void displayGraphicalPerformanceReport(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate,String region) {
    	Stage reportStage = new Stage();
        reportStage.setTitle("Graphical performance report of "+region+"\n");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Pie chart for on-time versus late deliveries
        PieChart deliveryPieChart = createDeliveryPieChart(reportData);
        
        // Performance bar chart by days of the week
        BarChart<String, Number> performanceByDayChart = createPerformanceByDayChart(reportData);
        
        // Line chart for average delivery times
       //Label avgDeliveryTimeLabel = createAvgDeliveryTimeLabel(reportData);

        Label summaryLabel = new Label(createSummaryText(reportData, startDate, endDate));
        summaryLabel.setWrapText(true);

        root.getChildren().addAll(summaryLabel, deliveryPieChart, performanceByDayChart);

        Scene scene = new Scene(root, 800, 600);
        reportStage.setScene(scene);
        reportStage.show();
    }
    
    /**
     * Creates a PieChart to visualize the distribution of deliveries that arrived on time versus late.
     *
     * @param reportData A map containing report data, which should include:
     *                   - "totalDeliveries": Total number of deliveries (Integer).
     *                   - "onTimeDeliveries": Number of deliveries that arrived on time (Integer).
     * @return A {@link PieChart} object displaying the distribution of on-time versus late deliveries.
     */
    private PieChart createDeliveryPieChart(Map<String, Object> reportData) {
    	long totalDeliveries = 0;
    	long onTimeDeliveries = 0;
    	Object totalObj = reportData.get("totalDeliveries");
        Object onTimeObj = reportData.get("totalOnTimeDeliveries");

        if (totalObj instanceof Number) {
            totalDeliveries = ((Number) totalObj).longValue();
        }

        if (onTimeObj instanceof Number) {
            onTimeDeliveries = ((Number) onTimeObj).longValue();
        }

    	long lateDeliveries = totalDeliveries - onTimeDeliveries;

        PieChart.Data slice1 = new PieChart.Data("On Time", onTimeDeliveries);
        PieChart.Data slice2 = new PieChart.Data("On Late", lateDeliveries);

        PieChart chart = new PieChart();
        chart.getData().addAll(slice1, slice2);
        chart.setTitle("Distribution Of Shipments");

        return chart;
    }
    
    /**
     * Creates a BarChart to display the performance of deliveries by days of the week.
     *
     * @param reportData A map containing report data, which should include:
     *                   - "performanceByDay": A map where keys are days of the week (String) and values are percentages of deliveries on time (Double).
     * @return A {@link BarChart} object showing performance by days of the week.
     */
    private BarChart<String, Number> createPerformanceByDayChart(Map<String, Object> reportData) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Performance by days of the week");
        xAxis.setLabel("Day in the week");
        yAxis.setLabel("Percentage of deliveries on time");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        @SuppressWarnings("unchecked")
		Map<String, BigDecimal> performanceByDay = (Map<String, BigDecimal>) reportData.get("performanceByDay");
        for (Map.Entry<String, BigDecimal> entry : performanceByDay.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        return barChart;
   }
    
    /**
     * Generates a summary text for the performance report.
     *
     * @param reportData A map containing report data, which should include:
     *                   - "totalDeliveries": Total number of deliveries (Integer).
     *                   - "onTimeDeliveries": Number of deliveries that arrived on time (Integer).
     *                   - "avgDeliveryTime": Average delivery time in minutes (Double).
     *                   - "totalRevenue": Total revenue from deliveries (Double).
     * @param startDate The start date of the reporting period (LocalDate).
     * @param endDate The end date of the reporting period (LocalDate).
     * @return A string containing the summary of the performance report.
     */
    private String createSummaryText(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate) {
    	 long totalDeliveries = 0;
    	    long onTimeDeliveries = 0;
    	    double avgDeliveryTime = 0.0;
    	    double totalRevenue = 0.0;

    	    Object totalObj = reportData.get("totalDeliveries");
    	    Object onTimeObj = reportData.get("totalOnTimeDeliveries");
    	    Object avgTimeObj = reportData.get("avgDeliveryTime");
    	    Object revenueObj = reportData.get("totalRevenue");

    	    if (totalObj instanceof Number) totalDeliveries = ((Number) totalObj).longValue();
    	    if (onTimeObj instanceof Number) onTimeDeliveries = ((Number) onTimeObj).longValue();
    	    if (avgTimeObj instanceof Number) avgDeliveryTime = ((Number) avgTimeObj).doubleValue();
    	    if (revenueObj instanceof Number) totalRevenue = ((Number) revenueObj).doubleValue();

    	    double onTimePercentage = totalDeliveries > 0 ? (double) onTimeDeliveries / totalDeliveries * 100 : 0;

    	    return String.format("Report period: %s - %s\n" +
    	                         "Total shipments: %d\n" +
    	                         "Deliveries arrived on time: %d (%.2f%%)\n" +
    	                         "Average delivery time: %.2f minutes\n" +
    	                         "Total revenues from deliveries: ₪%.2f",
    	            startDate, endDate, totalDeliveries, onTimeDeliveries, 
    	            onTimePercentage, avgDeliveryTime, totalRevenue);
    }

    /**
     * Displays an income report with detailed revenue information.
     *
     * @param reportData A map containing report data, which should include:
     *                   - "totalIncome": Total revenue (Double).
     *                   - "incomeByRestaurant": A map where keys are restaurant names (String) and values are total income from each restaurant (Double).
     *                   - "dailyIncome": A map where keys are dates (LocalDate) and values are daily income (Double).
     * @param startDate The start date of the reporting period (LocalDate).
     * @param endDate The end date of the reporting period (LocalDate).
     */
    private void displayIncomeReport(Map<String, Object> reportData, LocalDate startDate, LocalDate endDate,String region) {
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Income Report of "+ region+"\n");
        reportContent.append("===========\n\n");
        reportContent.append(String.format("The period of report: %s - %s\n\n", startDate, endDate));

        double totalIncome = (double) reportData.get("totalIncome");
        reportContent.append(String.format("Total revenue\r\n" + " : ₪%.2f\n\n", totalIncome));

        @SuppressWarnings("unchecked")
		Map<String, Double> incomeByRestaurant = (Map<String, Double>) reportData.get("incomeByRestaurant");
        reportContent.append("Revenues by restaurants:\n");
        for (Map.Entry<String, Double> entry : incomeByRestaurant.entrySet()) {
            reportContent.append(String.format("%s: ₪%.2f\n", entry.getKey(), entry.getValue()));
        }
        reportContent.append("\n");

        
        @SuppressWarnings("unchecked")
		Map<LocalDate, Double> dailyIncome = (Map<LocalDate, Double>) reportData.get("dailyIncome");
        reportContent.append("Daily Revenues:\n");
        for (Map.Entry<LocalDate, Double> entry : dailyIncome.entrySet()) {
            reportContent.append(String.format("%s: ₪%.2f\n", entry.getKey(), entry.getValue()));
        }

        //show report
        showReportDialog("Report Revenues", reportContent.toString());
    }
    
    /**
     * Displays a report in a dialog box.
     *
     * @param title The title of the report dialog.
     * @param content The content of the report to be displayed.
     */
    private void showReportDialog(String title, String content) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
    
    /**
     * Checks if the given date range is valid.
     * A valid date range has a start date not after the end date,
     * and both dates are in the same month and year.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return true if the date range is valid, false otherwise.
     */
    private boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null &&
               !endDate.isBefore(startDate) &&
               startDate.getMonth() == endDate.getMonth() &&
               startDate.getYear() == endDate.getYear();
    }

    /**
     * Validates the selected dates in the date pickers and updates the UI accordingly.
     * If the end date is invalid (before start date or in a different month/year),
     * the end date picker is highlighted in red.
     *
     * @param startDatePicker The DatePicker for the start date.
     * @param endDatePicker The DatePicker for the end date.
     */
    private void validateDates(DatePicker startDatePicker, DatePicker endDatePicker) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null) {
            if (!isValidDateRange(startDate, endDate)) {
                endDatePicker.setStyle("-fx-border-color: red;");
            } else {
                endDatePicker.setStyle("");
            }
        } else {
            endDatePicker.setStyle("");
        }
    }

    /**
     * Displays an alert dialog with the specified title and content.
     *
     * @param title The title of the alert dialog.
     * @param content The content message to be displayed in the alert.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Creates and returns a StringConverter for converting between LocalDate objects and their string representations.
     * This converter uses the format "dd/MM/yyyy" for date strings.
     *
     * @return A StringConverter that converts between LocalDate and String.
     */
    private StringConverter<LocalDate> getDateConverter() {
        return new StringConverter<LocalDate>() {
            private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
    }
    
    /**
     * Handles the back button action.
     * Closes the current window and shows the manager screen.
     */
    @FXML
    private void handleBack() {
        if (stage != null) {
            stage.close();
        }
        if (managerStage != null) {
            managerStage.show();
        } else {
            System.out.println("Warning: Manager stage is null");
        }
    }
}