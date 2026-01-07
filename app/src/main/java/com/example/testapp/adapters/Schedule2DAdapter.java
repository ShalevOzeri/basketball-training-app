package com.example.testapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.testapp.models.TimeSlot;
import com.example.testapp.models.Training;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Adapter for 2D schedule grid layout
 * Rows = Time slots (30-minute intervals)
 * Columns = Days of the week
 */
public class Schedule2DAdapter {

    private Context context;
    private TableLayout tableLayout;
    private Calendar weekStartDate;
    private List<TimeSlot> allTimeSlots;
    private Map<String, Training> trainingBySlot; // key = "HH:mm_dayIndex"
    private Set<String> occupiedSlotKeys; // Track which slots are already occupied (for multi-row trainings)
    private String courtId; // Store court ID for TimeSlot creation
    private String courtName; // Store court name for TimeSlot creation
    private OnSlotClickListener slotClickListener;
    private OnOccupiedSlotClickListener occupiedSlotListener;
    private List<Integer> visibleDayIndices; // Which day columns to render

    public interface OnSlotClickListener {
        void onSlotClick(TimeSlot timeSlot);
    }

    public interface OnOccupiedSlotClickListener {
        void onOccupiedSlotClick(TimeSlot timeSlot);
    }

    public Schedule2DAdapter(Context context, TableLayout tableLayout, Calendar weekStartDate) {
        this.context = context;
        this.tableLayout = tableLayout;
        this.weekStartDate = (Calendar) weekStartDate.clone();
        this.allTimeSlots = new ArrayList<>();
        this.trainingBySlot = new HashMap<>();
        this.occupiedSlotKeys = new java.util.HashSet<>();
        this.visibleDayIndices = new java.util.ArrayList<>();
        for (int i = 0; i < 7; i++) {
            this.visibleDayIndices.add(i);
        }
    }

    public void setSlotClickListener(OnSlotClickListener listener) {
        this.slotClickListener = listener;
    }

    public void setOccupiedSlotListener(OnOccupiedSlotClickListener listener) {
        this.occupiedSlotListener = listener;
    }

    public void setCourtInfo(String courtId, String courtName) {
        this.courtId = courtId;
        this.courtName = courtName;
    }

    public void setVisibleDayIndices(List<Integer> dayIndices) {
        this.visibleDayIndices = new java.util.ArrayList<>(dayIndices);
    }

    public void updateData(List<TimeSlot> timeSlots, Map<String, Training> trainings) {
        android.util.Log.d("Schedule2DAdapter", "updateData called: timeSlots=" + (timeSlots != null ? timeSlots.size() : "null") + ", trainings=" + (trainings != null ? trainings.size() : "null"));
        this.allTimeSlots = new ArrayList<>(timeSlots != null ? timeSlots : new ArrayList<>());
        this.trainingBySlot = new HashMap<>(trainings != null ? trainings : new HashMap<>());
        this.occupiedSlotKeys = new java.util.HashSet<>();
        android.util.Log.d("Schedule2DAdapter", "About to call rebuildTable");
        rebuildTable();
    }

    private void rebuildTable() {
        tableLayout.removeAllViews();

        android.util.Log.d("Schedule2DAdapter", "rebuildTable: totalTimeSlots=" + allTimeSlots.size() + ", trainingBySlot size=" + trainingBySlot.size());

        // Header row with day names
        addHeaderRow();

        // Extract unique time slots and sort them
        java.util.Set<String> uniqueTimes = new java.util.TreeSet<>();
        for (TimeSlot timeSlot : allTimeSlots) {
            uniqueTimes.add(timeSlot.getStartTime());
        }

        android.util.Log.d("Schedule2DAdapter", "Unique times: " + uniqueTimes.size());

        // Mark occupied slots (for multi-row trainings)
        occupiedSlotKeys.clear();
        for (Map.Entry<String, Training> entry : trainingBySlot.entrySet()) {
            String slotKey = entry.getKey(); // format: "HH:mm_dayIndex"
            Training training = entry.getValue();
            
            int rowSpan = calculateRowSpan(training);
            if (rowSpan > 1) {
                // Mark all continuation time slots as occupied
                List<String> timeSlots = getTimeSlotRange(training.getStartTime(), rowSpan);
                for (int i = 1; i < timeSlots.size(); i++) {
                    occupiedSlotKeys.add(timeSlots.get(i) + "_" + slotKey.substring(slotKey.lastIndexOf("_")));
                    android.util.Log.d("Schedule2DAdapter", "Marking slot as occupied: " + timeSlots.get(i));
                }
            }
        }

        // Time slot rows - create row for each time slot (don't skip any)
        for (String timeStr : uniqueTimes) {
            addTimeSlotRow(timeStr);
        }        
        android.util.Log.d("Schedule2DAdapter", "rebuildTable complete: added " + uniqueTimes.size() + " rows to table");    }

    private void addTimeSlotRow(String timeStr) {
        TableRow row = new TableRow(context);
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Time label cell
        TextView timeCell = new TextView(context);
        timeCell.setText(timeStr);
        timeCell.setPadding(12, 16, 12, 16);
        timeCell.setTypeface(null, Typeface.BOLD);
        timeCell.setTextSize(12);
        timeCell.setBackgroundColor(0xFFF5F5F5);
        timeCell.setGravity(android.view.Gravity.CENTER);
        
        // Use 0dp width with weight for responsive layout - narrower time column, taller cells
        TableRow.LayoutParams timeParams = new TableRow.LayoutParams(0, 120);
        timeParams.weight = 0.5f;  // Narrower time column
        timeCell.setLayoutParams(timeParams);
        row.addView(timeCell);

        // Day columns with training slots
        for (int dayIndex : visibleDayIndices) {
            Calendar dayCalendar = (Calendar) weekStartDate.clone();
            dayCalendar.add(Calendar.DATE, dayIndex);
            long dayTimestamp = dayCalendar.getTimeInMillis();

            // Find training for this specific time and day
            String slotKey = timeStr + "_" + dayIndex;
            Training training = trainingBySlot.get(slotKey);

            // Check if this slot is occupied by a multi-row training from earlier
            boolean isOccupied = occupiedSlotKeys.contains(slotKey);
            
            int rowSpan = training != null ? calculateRowSpan(training) : 1;

            if (training != null) {
                android.util.Log.d("Schedule2DAdapter", "Found training for " + slotKey + ": " + training.getTeamName() + " (Duration: " + training.getStartTime() + "-" + training.getEndTime() + ", rowSpan=" + rowSpan + ")");
            }

            // Create the cell
            if (isOccupied) {
                // This slot is part of a multi-row training from above - skip it
                android.util.Log.d("Schedule2DAdapter", "Skipping occupied slot: " + slotKey);
                continue;
            } else {
                // Create normal slot cell (either with training or empty)
                TextView slotCell = createSlotCellWithSpan(timeStr, dayTimestamp, dayIndex, training, rowSpan);
                
                // Use 0dp width with weight=1 for equal distribution among day columns, taller cells
                TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0, 120);
                cellParams.weight = 1.0f;
                
                // Apply negative margin for multi-row trainings
                if (training != null && rowSpan > 1) {
                    cellParams.bottomMargin = -(120 * (rowSpan - 1));
                    android.util.Log.d("Schedule2DAdapter", "Applied bottomMargin=" + cellParams.bottomMargin + " to multi-row cell");
                }
                
                slotCell.setLayoutParams(cellParams);
                row.addView(slotCell);
            }
        }

        tableLayout.addView(row);
    }
    
    /**
     * Create a cell that can span multiple rows using negative margins
     */
    private TextView createSlotCellWithSpan(String timeStr, long dayTimestamp, int dayIndex, Training training, int rowSpan) {
        TextView cell = new TextView(context);
        
        // Basic cell styling - no fixed dimensions, let layout params handle it
        cell.setPadding(8, 12, 8, 12);
        cell.setTextSize(11);
        cell.setGravity(android.view.Gravity.CENTER);

        if (training != null) {
            // Occupied slot
            StringBuilder trainingText = new StringBuilder(training.getTeamName());
            if (rowSpan > 1) {
                trainingText.append("\n").append(training.getStartTime()).append("-").append(training.getEndTime());
            }
            cell.setText(trainingText.toString());
            cell.setTextColor(0xFFFFFFFF); // White text
            cell.setTypeface(null, Typeface.BOLD);

            // Use team color if available
            if (training.getTeamColor() != null && !training.getTeamColor().isEmpty()) {
                try {
                    int color = android.graphics.Color.parseColor(training.getTeamColor());
                    cell.setBackgroundColor(color);
                } catch (Exception e) {
                    cell.setBackgroundColor(0xFF4CAF50); // Default green
                }
            } else {
                cell.setBackgroundColor(0xFF4CAF50); // Default green
            }

            // Make occupied cells clickable for editing
            cell.setClickable(true);
            cell.setOnClickListener(v -> {
                if (occupiedSlotListener != null) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setDate(dayTimestamp);
                    timeSlot.setStartTime(timeStr);
                    timeSlot.setTraining(training);
                    occupiedSlotListener.onOccupiedSlotClick(timeSlot);
                }
            });
            
            // Note: Layout params with negative margin will be set by caller in addSlotRow

        } else {
            // Empty slot
            cell.setText("+");
            cell.setTextColor(0xFF999999);
            cell.setBackgroundColor(0xFFFAFAFA);

            cell.setClickable(true);
            cell.setOnClickListener(v -> {
                if (slotClickListener != null) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setDate(dayTimestamp);
                    timeSlot.setStartTime(timeStr);
                    timeSlot.setCourtId(courtId);
                    timeSlot.setCourtName(courtName);
                    slotClickListener.onSlotClick(timeSlot);
                }
            });
        }

        return cell;
    }

    /**
     * Calculate how many 30-minute slots a training occupies
     */
    private int calculateRowSpan(Training training) {
        if (training == null || training.getStartTime() == null || training.getEndTime() == null) {
            return 1;
        }
        
        try {
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
            long startMs = timeFormat.parse(training.getStartTime()).getTime();
            long endMs = timeFormat.parse(training.getEndTime()).getTime();
            long durationMs = endMs - startMs;
            
            // Each slot is 30 minutes (1800000 ms)
            int slots = (int) Math.ceil((double) durationMs / 1800000);
            return Math.max(1, slots);
        } catch (Exception e) {
            android.util.Log.e("Schedule2DAdapter", "Error calculating row span", e);
            return 1;
        }
    }

    /**
     * Get list of time slots for a training duration
     */
    private List<String> getTimeSlotRange(String startTime, int rowSpan) {
        List<String> slots = new ArrayList<>();
        try {
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());
            java.util.Date startDate = timeFormat.parse(startTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            slots.add(timeFormat.format(cal.getTime()));
            for (int i = 1; i < rowSpan; i++) {
                cal.add(Calendar.MINUTE, 30);
                slots.add(timeFormat.format(cal.getTime()));
            }
        } catch (Exception e) {
            android.util.Log.e("Schedule2DAdapter", "Error getting time slot range", e);
            slots.add(startTime);
        }
        return slots;
    }

    private void addHeaderRow() {
        android.util.Log.d("Schedule2DAdapter", "addHeaderRow: visibleDayIndices.size()=" + visibleDayIndices.size());
        TableRow headerRow = new TableRow(context);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Corner cell (time label)
        TextView timeLabel = new TextView(context);
        timeLabel.setText("שעה");
        timeLabel.setPadding(12, 16, 12, 16);
        timeLabel.setTypeface(null, Typeface.BOLD);
        timeLabel.setTextSize(13);
        timeLabel.setBackgroundColor(0xFFF0F0F0);
        timeLabel.setGravity(android.view.Gravity.CENTER);
        
        // Use 0dp width with weight for responsive layout - narrower time column
        TableRow.LayoutParams timeLabelParams = new TableRow.LayoutParams(0, 120);
        timeLabelParams.weight = 0.5f;  // Narrower time column
        timeLabel.setLayoutParams(timeLabelParams);
        headerRow.addView(timeLabel);

        // Day columns
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE\ndd/MM", Locale.getDefault());
        for (int i : visibleDayIndices) {
            android.util.Log.d("Schedule2DAdapter", "Adding header for dayIndex=" + i);
            Calendar dayCalendar = (Calendar) weekStartDate.clone();
            dayCalendar.add(Calendar.DATE, i);

            TextView dayHeader = new TextView(context);
            dayHeader.setText(dayFormat.format(dayCalendar.getTime()));
            dayHeader.setPadding(12, 16, 12, 16);
            dayHeader.setTypeface(null, Typeface.BOLD);
            dayHeader.setTextSize(12);
            dayHeader.setBackgroundColor(0xFFE8E8E8);
            dayHeader.setGravity(android.view.Gravity.CENTER);
            dayHeader.setMaxLines(2);
            
            // Use 0dp width with weight=1 for equal distribution, taller header
            TableRow.LayoutParams headerParams = new TableRow.LayoutParams(0, 120);
            headerParams.weight = 1.0f;
            dayHeader.setLayoutParams(headerParams);

            headerRow.addView(dayHeader);
        }

        tableLayout.addView(headerRow);
        android.util.Log.d("Schedule2DAdapter", "addHeaderRow complete");
    }

    private TextView createSlotCell(String timeStr, long dayTimestamp, int dayIndex, Training training, int rowSpan) {
        TextView cell = new TextView(context);
        
        // Use provided rowSpan for training duration
        int cellHeight = 80 * rowSpan;
        
        cell.setMinimumHeight(cellHeight);
        cell.setHeight(cellHeight);
        cell.setMinimumWidth(100);
        cell.setPadding(8, 12, 8, 12);
        cell.setTextSize(11);
        cell.setGravity(android.view.Gravity.CENTER);

        if (training != null) {
            // Occupied slot
            StringBuilder trainingText = new StringBuilder(training.getTeamName());
            if (rowSpan > 1) {
                trainingText.append("\n").append(training.getStartTime()).append("-").append(training.getEndTime());
            }
            cell.setText(trainingText.toString());
            cell.setTextColor(0xFFFFFFFF); // White text
            cell.setTypeface(null, Typeface.BOLD);

            // Use team color if available
            if (training.getTeamColor() != null && !training.getTeamColor().isEmpty()) {
                try {
                    int color = android.graphics.Color.parseColor(training.getTeamColor());
                    cell.setBackgroundColor(color);
                } catch (Exception e) {
                    cell.setBackgroundColor(0xFF4CAF50); // Default green
                }
            } else {
                cell.setBackgroundColor(0xFF4CAF50); // Default green
            }

            // Make occupied cells clickable for editing
            cell.setClickable(true);
            cell.setOnClickListener(v -> {
                if (occupiedSlotListener != null) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setDate(dayTimestamp);
                    timeSlot.setStartTime(timeStr);
                    timeSlot.setTraining(training);
                    occupiedSlotListener.onOccupiedSlotClick(timeSlot);
                }
            });

        } else {
            // Empty slot
            cell.setText("+");
            cell.setTextColor(0xFF999999);
            cell.setBackgroundColor(0xFFFAFAFA);

            cell.setClickable(true);
            cell.setOnClickListener(v -> {
                if (slotClickListener != null) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setDate(dayTimestamp);
                    timeSlot.setStartTime(timeStr);
                    timeSlot.setCourtId(courtId);
                    timeSlot.setCourtName(courtName);
                    slotClickListener.onSlotClick(timeSlot);
                }
            });
        }

        return cell;
    }
}