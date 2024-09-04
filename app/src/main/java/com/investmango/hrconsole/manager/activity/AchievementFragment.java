package com.investmango.hrconsole.manager.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.FragmentAchievementBinding;
import com.investmango.hrconsole.model.EmpPerformance;
import com.investmango.hrconsole.model.MonthlyPerformanceResp;
import com.investmango.hrconsole.service.DateAndTimeUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementFragment extends Fragment {
    private FragmentAchievementBinding binding;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    String ViewOf, authority;
    AwesomeProgressDialog progressDialog;

    MonthlyPerformanceResp progressResp;

    List<EmpPerformance> empPerformanceList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        authority = preferences.getString("Authority", "user");

        progressDialog = new AwesomeProgressDialog(getContext());
        progressDialog.addTitle("Loading...");// add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS);

        assert getArguments() != null;
        if (getArguments().containsKey("childUserid")) {
            ViewOf = getArguments().getString("ViewOf");
            userId = getArguments().getLong("childUserid");
            Log.e("Achievements", "onCreate: " + userId);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_achievement, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ViewOf!=null && ViewOf.equals("child")) {
            binding.editButton.setVisibility(View.VISIBLE);
        }

        binding.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLayout("edit");
            }
        });


        binding.Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setLayout("update");

                if (binding.skillIncDec.getCurrentValue() > 5 || binding.teamIncDec.getCurrentValue() > 5 || binding.userReviewIncDec.getCurrentValue() > 5 || binding.assisantceIncDec.getCurrentValue() > 5 || binding.jobKnowledIncDec.getCurrentValue() > 5
                        || binding.attendanceIncDec.getCurrentValue() > 5 || binding.WorkdeliveryIncDec.getCurrentValue() > 5 || binding.generalIncDec.getCurrentValue() > 5 || binding.performanceIncDec.getCurrentValue() > 5 || binding.attendanceIncDec.getCurrentValue() > 5) {

                    Toast.makeText(requireContext(), "Give Reviews out of 5.", Toast.LENGTH_LONG).show();
                } else {
                    EmpPerformance performance = new EmpPerformance();

                    performance.setId(empPerformanceList
                            .get(0).getId());
                    performance.setAttendance(binding.attendanceIncDec.getCurrentValue());
                    performance.setWorkQuality(binding.workQualityIncDec.getCurrentValue());
                    performance.setJobKnowledge(binding.jobKnowledIncDec.getCurrentValue());
                    performance.setTeamWork(binding.teamIncDec.getCurrentValue());
                    performance.setGeneralConduct(binding.generalIncDec.getCurrentValue());
                    binding.generalIncDec.setValue(5);
                    saveUserPerformance(performance);
                }
            }
        });

        fetchPerformanceReport();
        getEmployeePerformanceList(token, userId);
    }

    private void setLayout(String doo) {
        if (doo.equals("edit")) {
            binding.graphLaout.setVisibility(View.GONE);
            binding.progressLayout.setVisibility(View.GONE);
            binding.editLayout.setVisibility(View.VISIBLE);
        } else if (doo.equals("update")) {
            binding.graphLaout.setVisibility(View.VISIBLE);
            binding.progressLayout.setVisibility(View.VISIBLE);
            binding.editLayout.setVisibility(View.GONE);
        }
    }

    private void populatePieChart(long totalPresent, long totalAbsent, PieChart pieChart) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(totalPresent, "llll"));
        pieEntries.add(new PieEntry(totalAbsent, "llll"));


        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Employee Attendance");
        // on below line we are setting icons.
        pieDataSet.setDrawIcons(false);

        pieDataSet.setColors(
                ContextCompat.getColor(requireContext(), R.color.skyBlue),
                ContextCompat.getColor(requireContext(), R.color.greyForChart)
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
        pieChart.setCenterText("Obtained score\n" + totalPresent);
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
    private void lineGraph(List<Integer> months,List<Float> score) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i=0;i<months.size();i++){
            entries.add(new Entry(months.get(i),score.get(i)));
            Log.e("linegraph", months.get(0)+"  "+ score.get(0));

        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Sample Data");
        lineDataSet.setColor(Color.BLUE); // Line color
        lineDataSet.setDrawFilled(true); // Enable filling
        lineDataSet.setFillColor(Color.BLUE); // Fill color
        lineDataSet.setFillAlpha(70); // Fill transparency

        lineDataSet.setDrawCircles(false);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setDrawValues(false);
        lineDataSet.setCircleRadius(0);

        //to make the smooth line as the graph is adrapt change so smooth curve
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //to enable the cubic density : if 1 then it will be sharp curve
        lineDataSet.setCubicIntensity(0.2f);   //to make the smooth line as the graph is adapt change so smooth curve


        // Combine the two data sets
        LineData lineData = new LineData(lineDataSet);
        binding.lineChart.setData(lineData);
        binding.lineChart.invalidate();


        // Optional: Customize the chart
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = binding.lineChart.getAxisLeft();
        YAxis rightYAxis = binding.lineChart.getAxisRight();
        leftAxis.setTextColor(Color.WHITE);
        rightYAxis.setTextColor(Color.WHITE);

        leftAxis.setAxisMinimum(0); // Start at 0

        lineDataSet.setLineWidth(2f); // Set line width
//        lineDataSet.setFillColor(getResources().getColor(R.drawable.fill_chart)); // Change fill color
        lineDataSet.setDrawFilled(true);

        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fill_chart);
        lineDataSet.setFillDrawable(drawable);

        lineDataSet.setFillAlpha(100); // Change fill transparency
        xAxis.setDrawGridLines(false); // Disable X-axis grid lines
        leftAxis.setDrawGridLines(false); // Disable Y-axis grid lines

    }

    private void saveUserPerformance(EmpPerformance attendance) {
        Log.d("UserAttendanceFragment", "userId: " + userId);
        Call<EmpPerformance> call = apiInterface.updatePerformance(attendance, preferences.getLong("userId", 0));
        call.enqueue(new Callback<EmpPerformance>() {
            @Override
            public void onResponse(@NonNull Call<EmpPerformance> call, @NonNull Response<EmpPerformance> response) {
                if (response.code() == 201) {
                    Toast.makeText(requireContext(), "Performance Updated.", Toast.LENGTH_LONG).show();
                    empPerformanceList.clear();
                    empPerformanceList.add(response.body());
                    setUpData();
                } else {
                    Log.e("failure", "onResponse: "+response.message() );
                    Toast.makeText(getActivity(), "Server error " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmpPerformance> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Server error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }
    private void fetchPerformanceReport() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();

        Call<MonthlyPerformanceResp> call = apiInterface.getMonthPerformance(userId);
        call.enqueue(new Callback<MonthlyPerformanceResp>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Response<MonthlyPerformanceResp> response) {
                if (response.isSuccessful()) {
                    progressResp = response.body();

                    if (progressResp!=null && progressResp.getMom()!=null){
                        List<Integer> months= new ArrayList<>();
                        List<Float> score=new ArrayList<>();

                        for (int i =0; i<progressResp.getProgressData().size(); i++) {
                            months.add(DateAndTimeUtility.getMonthNumber(progressResp.getProgressData().get(i).getMonth()));

                            String formattedNumber = String.format("%.2f", progressResp.getProgressData().get(i).getScore());
                            score.add(Float.parseFloat(formattedNumber));

                        }
                        lineGraph(months,score);

                    }
                } else {
                    Toast.makeText(getContext(), response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    private void getEmployeePerformanceList(String token, long userId) {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        progressDialog.showDialog();
        Call<List<EmpPerformance>> call = apiInterface.getSingleEmployeeAllPerformanceByEmpId(token, userId);
        call.enqueue(new Callback<List<EmpPerformance>>() {
            @Override
            public void onResponse(@NonNull Call<List<EmpPerformance>> call, @NonNull Response<List<EmpPerformance>> response) {
                progressDialog.dismissDialog();
                if (response.isSuccessful()) {
                    empPerformanceList = response.body();
                    if (empPerformanceList != null && !empPerformanceList.isEmpty()) {
                        setUpData();
                    } else {
                        Log.e("EmployeePerformance", "EmpPerformance list is null or empty");
                    }
                } else {
                    Log.e("EmployeePerformance", "Failed to fetch empPerformance list");
                }


            }

            @Override
            public void onFailure(Call<List<EmpPerformance>> call, Throwable t) {
                progressDialog.dismissDialog();
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    private long getPercentage(float value) {
        return (long) (value / 5 * 100);
    }

    private void setUpData() {
        Log.e("percentage", "setUpData: " + getPercentage(3));
        binding.attendanceProg.setProgress((int) getPercentage(empPerformanceList.get(0).getAttendance()));
        binding.generalProg.setProgress((int) getPercentage(empPerformanceList.get(0).getGeneralConduct()));
        binding.jobKnowProg.setProgress((int) getPercentage(empPerformanceList.get(0).getJobKnowledge()));
        binding.teamWorkprog.setProgress((int) getPercentage(empPerformanceList.get(0).getTeamWork()));
        binding.skillProg.setProgress((int) getPercentage(empPerformanceList.get(0).getSkills()));


        binding.attendanceIncDec.setMiddleText(empPerformanceList.get(0).getAttendance().toString());
        binding.attendanceIncDec.setValue(Integer.parseInt(empPerformanceList.get(0).getAttendance().toString()));

        binding.generalIncDec.setValue((int) empPerformanceList.get(0).getGeneralConduct());
        binding.generalIncDec.setMiddleText(String.valueOf(empPerformanceList.get(0).getGeneralConduct()));

        binding.jobKnowledIncDec.setValue((int) empPerformanceList.get(0).getJobKnowledge());
        binding.jobKnowledIncDec.setMiddleText(String.valueOf(empPerformanceList.get(0).getJobKnowledge()));

        binding.teamIncDec.setValue((int) empPerformanceList.get(0).getTeamWork());
        binding.teamIncDec.setMiddleText(String.valueOf(empPerformanceList.get(0).getTeamWork()));

        binding.skillIncDec.setValue((int) empPerformanceList.get(0).getSkills());
        binding.skillIncDec.setMiddleText(String.valueOf(empPerformanceList.get(0).getSkills()));


        binding.description.setText(empPerformanceList.get(0).getComment());
        binding.messgFrom.setText(empPerformanceList.get(0).getGivenByName());
        binding.dateTime.setText(DateAndTimeUtility.getDateAndTimeFromLong(empPerformanceList.get(0).getCreatedOn()));

        if (empPerformanceList.get(0).getObtainedScore() > 50) {
            binding.keepItUp.setVisibility(View.VISIBLE);
        }
        populatePieChart(empPerformanceList.get(0).getTotalScore(), empPerformanceList.get(0).getObtainedScore(), binding.pieChart);


    }
}