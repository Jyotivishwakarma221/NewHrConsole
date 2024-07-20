package com.investmango.hrconsole.admin.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.AdminSalaryDetails;

import java.text.DecimalFormat;
import java.util.List;

public class SalaryDetailAdapter extends RecyclerView.Adapter<SalaryDetailAdapter.LeadsViewHolder> {
    private final Context context;
    private List<AdminSalaryDetails> adminSalaryDetails;
    private SharedPreferences preferences;


    public SalaryDetailAdapter(List<AdminSalaryDetails> adminSalaryDetails, Context context) {
        this.adminSalaryDetails = adminSalaryDetails;
        this.context = context;
        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");
    }

    @NonNull
    @Override
    public SalaryDetailAdapter.LeadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.salary_details_adapter, parent, false);
        return new LeadsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryDetailAdapter.LeadsViewHolder holder, int position) {
        AdminSalaryDetails data = adminSalaryDetails.get(position);
        holder.id.setText(String.valueOf(data.getId()));
        holder.nameTex.setText(capitalizeFirstLetter(data.getUserName()));
        holder.basicss_TextView.setText(String.valueOf(data.getBasicSalary()));
        holder.hra.setText(String.valueOf(data.getHra()));
        holder.medical.setText(String.valueOf(data.getMedicalFund()));
        holder.bonus.setText(String.valueOf(data.getBonus()));
        holder.insentive.setText(String.valueOf(data.getIncentive()));
        holder.convience.setText(String.valueOf(data.getConvience()));
        holder.total_Salary.setText(String.valueOf(data.getTotalsalary()));
        holder.pay_Salary.setText(formatSalary(data.getMonthlysalary()));
    }

    @Override
    public int getItemCount() {
        return adminSalaryDetails.size();
    }

    private String capitalizeFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String formatSalary(double salary) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(salary);
    }

    public static class LeadsViewHolder extends RecyclerView.ViewHolder {
        TextView id, nameTex, basicss_TextView, hra, medical, bonus, insentive, convience, total_Salary, pay_Salary;

        public LeadsViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            nameTex = itemView.findViewById(R.id.nameTex);
            basicss_TextView = itemView.findViewById(R.id.basicss_TextView);
            hra = itemView.findViewById(R.id.hra);
            medical = itemView.findViewById(R.id.medical);
            bonus = itemView.findViewById(R.id.bonus);
            insentive = itemView.findViewById(R.id.insentive);
            convience = itemView.findViewById(R.id.convience);
            total_Salary = itemView.findViewById(R.id.total_Salary);
            pay_Salary = itemView.findViewById(R.id.pay_Salary);
        }
    }
}
