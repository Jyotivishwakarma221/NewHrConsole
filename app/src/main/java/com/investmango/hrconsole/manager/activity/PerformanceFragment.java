package com.investmango.hrconsole.manager.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.FragmentPerformanceBinding;
import com.investmango.hrconsole.model.MonthlyPerformanceResp;
import com.investmango.hrconsole.service.DateAndTimeUtility;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerformanceFragment extends Fragment {

    private FragmentPerformanceBinding binding;
    private long userId;
    String[] languages;
    private ApiInterface apiInterface;
    MonthlyPerformanceResp empPerformanceList;
    boolean firstMonthEncountered = false;


    private String token;
    String month, ViewOf;
    int monthNumber, maxMonthNumber;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        if (getArguments().containsKey("childUserid")) {
            ViewOf = getArguments().getString("ViewOf");
            userId = getArguments().getLong("childUserid");
            Log.e("Achievements", "onCreate: " + userId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_performance, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        languages = getResources().getStringArray(R.array.Months);

        if (ViewOf != null && ViewOf.equals("child")) {
            binding.linearlay2.setVisibility(View.VISIBLE);
            binding.linearlay.setVisibility(View.GONE);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate currentdate = LocalDate.now();
            Month thismonth = currentdate.getMonth();
            month = thismonth.toString();

            Log.e("performance", "onResponse: " + month);
        }
        binding.months.setAdapter(new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, languages));
        fetchPerformanceReport();


        binding.months.setOnItemClickListener((parent, view1, position, id) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                month = (String) parent.getItemAtPosition(position);
                fetchPerformanceReport();

            }
        });

        stepBar(new String[]{"", "", "", ""});
    }

    private void stepBar(String[] descriptionData) {
        binding.stepsView.setLabels(descriptionData);
        binding.stepsView.setBarColorIndicator(Color.BLACK);
        binding.stepsView.setProgressColorIndicator(getResources().getColor(R.color.greyOfEye));
        binding.stepsView.setLabelColorIndicator(getResources().getColor(R.color.orange));
        binding.stepsView.setCompletedPosition(0);
        binding.stepsView.drawView();
        if (descriptionData.length != 1)
            binding.stepsView.setCompletedPosition(descriptionData.length - 1);
        else binding.stepsView.setCompletedPosition(0);

    }

    private void populatePieChart(long totalPresent, long halfDay, long totalAbsent, long totaldays, long total, PieChart pieChart) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(totalPresent, "present"));
        pieEntries.add(new PieEntry(totalAbsent, "absent"));
        pieEntries.add(new PieEntry(halfDay, "halfDay"));
//        pieEntries.add(new PieEntry(total, "total"));
        if (totaldays != 0) {
            pieEntries.add(new PieEntry(totaldays, "futureDate"));

        }
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Employee Attendance");
        // on below line we are setting icons.
        pieDataSet.setDrawIcons(false);

        pieDataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.darkBlue),
                ContextCompat.getColor(requireContext(), R.color.strokeBlack),
                ContextCompat.getColor(requireContext(), R.color.yellow),
                ContextCompat.getColor(requireContext(), R.color.greyOfEye)

        );

        //draw value are the text that come on the pie slice
        pieDataSet.setDrawValues(false);

        //the small icons come in the chart are legends
        pieChart.getLegend().setEnabled(false);

        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setRotationAngle(0f);
        pieChart.animateY(1400, Easing.EaseInOutQuad);

        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(R.color.black);

        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(Color.WHITE);
//        pieChart.setTransparentCircleAlpha(110);

        // on  below line we are setting hole radius
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);

        // on below line we are setting center text
        pieChart.setDrawCenterText(true);
        Log.e("piechart", "setUpData: " + total);

        pieChart.setCenterText("Total \n " + total + "  ");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);


        PieData pieData = new PieData(pieDataSet);


        pieChart.setData(pieData);

        pieChart.setHoleRadius(50);
        pieChart.setTransparentCircleRadius(50);

        pieChart.getDescription().setEnabled(false);

        pieChart.invalidate();
    }

    @SuppressLint("ResourceType")
    private void lineGraph(List<Integer> month, List<Float> score) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();


        if (month.get(0) != 1)
            entries.add(new Entry(month.get(0) - 1, 0));
        else entries.add(new Entry(0, 0));

        for (int i = 0; i < month.size(); i++) {
            if (month.get(i) == 1) {
                firstMonthEncountered = true;
                entries2.add(new Entry(month.get(i), score.get(i)));
            } else {
                entries.add(new Entry(month.get(i), score.get(i)));
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate currentdate = LocalDate.now();
            monthNumber = currentdate.getMonthValue();
            LocalDate maxMonth = currentdate.minusMonths(6);
            maxMonthNumber = maxMonth.getMonthValue();
            Log.e("maxMonth", "lineGraph: " + monthNumber + " " + maxMonthNumber);

        }

        Log.e("entry", "lineGraph: " + entries);

        LineDataSet lineDataSet = new LineDataSet(entries, "This Month");
        lineDataSet.setColor(Color.BLUE); // Line color
        lineDataSet.setDrawFilled(true); // Enable filling
        lineDataSet.setFillColor(Color.BLUE); // Fill color
        lineDataSet.setFillAlpha(70); // Fill transparency
        lineDataSet.setValueTextColor(Color.WHITE); // Change to your preferred color
        lineDataSet.setValueTextSize(9f); // Set text size
        lineDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        binding.lineChart.setDoubleTapToZoomEnabled(false); // for double tap zooming.
        binding.lineChart.setScaleEnabled(false); // for two finger zooming


//        entries2.add(new Entry(0, 20));
//        entries2.add(new Entry(1, 30));
//        entries2.add(new Entry(2, 40));
//        entries2.add(new Entry(3, 100));
//        entries2.add(new Entry(4, 70));

        LineDataSet lineDataSet2 = new LineDataSet(entries2, " Previous");
        lineDataSet2.setColor(Color.parseColor("#FFBD59")); // Line color
        lineDataSet2.setDrawFilled(false); // Enable filling
        lineDataSet2.setFillColor(Color.YELLOW); // Fill color
        lineDataSet.setValueTextColor(Color.WHITE); // Change to your preferred color
        lineDataSet2.setFillAlpha(50); // Fill transparency

        // Use a custom value formatter to set the color
        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value); // Format value to 2 decimal places
            }
        });
        lineDataSet2.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value); // Format value to 2 decimal places
            }
        });
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet2.setValueTextColor(Color.WHITE);


        lineDataSet.setDrawCircles(false);
        lineDataSet2.setDrawCircles(false);
        lineDataSet.setCircleRadius(0);

        //to make the smooth line as the graph is adapt change so smooth curve
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //to enable the cubic density : if 1 then it will be sharp curve
        lineDataSet.setCubicIntensity(0.2f);//to make the smooth line as the graph is adapt change so smooth curve

        lineDataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //to enable the cubic density : if 1 then it will be sharp curve
        lineDataSet2.setCubicIntensity(0.2f);


        // Combine the two data sets
        LineData lineData = new LineData(lineDataSet, lineDataSet2);
        binding.lineChart.setData(lineData);
        binding.lineChart.invalidate();


        // Optional: Customize the chart
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        if (month.get(0) != 0)
            xAxis.setAxisMinimum(maxMonthNumber + 1);
        xAxis.setAxisMaximum(monthNumber + 1); // Set Y-axis maximum value
        xAxis.setLabelCount(6, true); // 11 steps for labels (0, 10, 20, ..., 100)
        xAxis.setGranularity(1f);


        YAxis leftAxis = binding.lineChart.getAxisLeft();
        YAxis rightYAxis = binding.lineChart.getAxisRight();
        leftAxis.setTextColor(Color.WHITE);
        rightYAxis.setTextColor(Color.WHITE);
        rightYAxis.setEnabled(false); // Disable the right Y-axis if not needed

        Legend legend = binding.lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        binding.lineChart.getAxisRight().setEnabled(true); // Disable right Y-axis

        leftAxis.setAxisMinimum(0);// Start at 0
        leftAxis.setAxisMaximum(100); // Set Y-axis maximum value
        leftAxis.setLabelCount(11, true); // 11 steps for labels (0, 10, 20, ..., 100)
        leftAxis.setGranularity(10f); // Set interval to 10


        lineDataSet.setLineWidth(2f); // Set line width
//        lineDataSet.setFillColor(getResources().getColor(R.drawable.fill_chart)); // Change fill color
        lineDataSet.setDrawFilled(true);

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fill_chart);
        lineDataSet.setFillDrawable(drawable);

        lineDataSet.setFillAlpha(100); // Change fill transparency
        xAxis.setDrawGridLines(false); // Disable X-axis grid lines
        leftAxis.setDrawGridLines(false); // Disable Y-axis grid lines

    }

    private void fetchPerformanceReport() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        Log.e("performance", "onResponse: 1 ");

        Call<MonthlyPerformanceResp> call = apiInterface.getMonthPerformance(userId, month.toString().toLowerCase());
        call.enqueue(new Callback<MonthlyPerformanceResp>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Response<MonthlyPerformanceResp> response) {
                if (response.isSuccessful()) {
                    empPerformanceList = response.body();

                    if (empPerformanceList != null && empPerformanceList.getTimeline() != null) {
                        int size = empPerformanceList.getTimeline().size();
                        Log.e("performance", "onResponse: " + size);

                        if (size > 0) {
                            String[] descriptionData = new String[size];
                            for (int i = 0; i < size; i++) {
                                descriptionData[i] = empPerformanceList.getTimeline().get(i).getAchievement();
                                Log.e("performance", "onResponse: " + descriptionData[i]);
                            }
                            try {
                                if (descriptionData.length != 0 || descriptionData != null)
                                    stepBar(descriptionData);
                                setUpData();
                            } catch (IndexOutOfBoundsException e) {
                                Log.e("performance", "IndexOutOfBoundsException caught: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("performance", "Timeline data is empty");
                        }
                    }
                    if (empPerformanceList != null && empPerformanceList.getMom() != null) {
                        List<Integer> months = new ArrayList<>();
                        List<Float> score = new ArrayList<>();

                        for (int i = 0; i < empPerformanceList.getMom().size(); i++) {
                            months.add(DateAndTimeUtility.getMonthNumber(empPerformanceList.getMom().get(i).getMonth()));
                            Log.e("months", "onResponse: " + DateAndTimeUtility.getMonthNumber(empPerformanceList.getMom().get(i).getMonth()));
                            String formattedNumber = String.format("%.2f", empPerformanceList.getMom().get(i).getScore());
                            score.add(Float.parseFloat(formattedNumber));

                        }
                        if (months.size() != 0 || score.size() != 0)
                            lineGraph(months, score);

//                        Log.e("StepsView", months.get(0)+"  "+ score.get(0));
                    }
                } else {
                    Toast.makeText(getContext(), getErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    private void setUpData() {
        binding.performancePercent.setText(String.format("%.2f", empPerformanceList.getOverAllPerformace())+ "%");

        binding.PendingCount.setText(empPerformanceList.getPendingTasks().toString());
        binding.AssignedCount.setText(empPerformanceList.getAssignedTasks().toString());
        binding.DoneCount.setText(empPerformanceList.getTotalTasks().toString());

        binding.PendingCount2.setText(empPerformanceList.getPendingTasks().toString());
        binding.AssignedCount2.setText(empPerformanceList.getAssignedTasks().toString());
        binding.DoneCount2.setText(empPerformanceList.getTotalTasks().toString());


        binding.Attendanceday.setText(empPerformanceList.getPresentDays().toString());
        binding.Halfday.setText(empPerformanceList.getHalfDay().toString());
        binding.Absentday.setText(empPerformanceList.getAbsent().toString());
        binding.MeetingAbsent.setText(empPerformanceList.getAbsentMeetings().toString());
        binding.MeetingPresence.setText(empPerformanceList.getPresentMeetings().toString());
        binding.MeetingPresence.setText(empPerformanceList.getPresentMeetings().toString());
        binding.totalMeetings.setText(empPerformanceList.getTotalMeetings().toString());


        binding.completionRate.setText(empPerformanceList.getTaskCompletionRatio().toString());
        Log.e("piechart", "setUpData: " + empPerformanceList.getTotalWorkingDays());

        populatePieChart(empPerformanceList.getPresentDays(), empPerformanceList.getHalfDay(), empPerformanceList.getAbsent(), empPerformanceList.getTotalWorkingDays(), empPerformanceList.getAbsentDays(), binding.pieChart);
        populatePieChart(empPerformanceList.getPresentMeetings(), empPerformanceList.getAbsentMeetings(), empPerformanceList.getTotalMeetings(), 0, empPerformanceList.getTotalMeetings(), binding.pieChart2);


    }

    private String getErrorMessage(Response<MonthlyPerformanceResp> response) {
        String errorMessage = "Unknown error";
        try {
            JSONObject errorJson = new JSONObject(response.errorBody().string());
            errorMessage = errorJson.optString("message", errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

}