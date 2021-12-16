package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.*;

import java.io.*;
import java.util.LinkedList;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        LinkedList <Thread> threadList = new LinkedList<>();
        File input = new File("C://Users//Assiph//IdeaProjects//SPL2//example_input.json");
        JsonElement fileElement = JsonParser.parseReader(new FileReader(input));
        JsonObject fileObject = fileElement.getAsJsonObject();
        LinkedList <StudentService> studentServiceList = new LinkedList();
        LinkedList <ConferenceService> cfsList = new LinkedList<>();
        int speed = fileObject.get("TickTime").getAsInt();
        int duration = fileObject.get("Duration").getAsInt();
        System.out.println(speed);
        System.out.println(duration);
        TimeService clock = new TimeService(speed,duration);
        Thread clockt = new Thread(clock);
        threadList.add(clockt);
        clockt.start();
        JsonArray jsonArrayOfStudents = fileObject.get("Students").getAsJsonArray();
        JsonArray jsonArrayGPU = fileObject.get("GPUS").getAsJsonArray();
        int i =0;
        for (JsonElement gpu : jsonArrayGPU){
            String type="";
            type = gpu.getAsString();
            GPU newGpu=null;
            if (type.equals("RTX3090")){
                newGpu = new GPU(GPU.Type.RTX3090);
            }
            if (type.equals("RTX2080"))
                newGpu = new GPU(GPU.Type.RTX2080);
            if (type.equals("GTX1080"))
                newGpu= new GPU(GPU.Type.GTX1080);
            System.out.println(newGpu.getType());
            GPUService gpus = new GPUService("Gpu Service" + i,newGpu);
            Thread gpuservicet = new Thread(gpus);
            threadList.add(gpuservicet);
            gpuservicet.start();
            i++;
        }
        i=0;
        JsonArray jsonArrayCPU = fileObject.get("CPUS").getAsJsonArray();
        for (JsonElement cpu : jsonArrayCPU){
            CPU newCpu = new CPU(cpu.getAsInt());
            System.out.println(newCpu.getCores());
            CPUService cpus = new CPUService("Cpu Service" + i,newCpu);
            Thread cpuservicet = new Thread(cpus);
            threadList.add(cpuservicet);
            cpuservicet.start();
            i++;
        }
        JsonArray jsonArrayConference = fileObject.get("Conferences").getAsJsonArray();
        for (JsonElement con : jsonArrayConference){
            JsonObject conferenceJsonObject = con.getAsJsonObject();
            String name = conferenceJsonObject.get("name").getAsString();
            int date = conferenceJsonObject.get("date").getAsInt();
            ConfrenceInformation cfi = new ConfrenceInformation(name,date);
            ConferenceService cfs = new ConferenceService(name + " service",cfi);
            cfsList.add(cfs);
            System.out.println(cfi.getName() + ":" + date);
            System.out.println(cfs.getName() + ":" + cfi.getDate());
            Thread conferencet = new Thread(cfs);
            threadList.add(conferencet);
            conferencet.start();
        }
        for (JsonElement st : jsonArrayOfStudents){
            JsonObject studentJsonObject = st.getAsJsonObject();
            String name = studentJsonObject.get("name").getAsString();
            String depertment = studentJsonObject.get("department").getAsString();
            String statusget = studentJsonObject.get("status").getAsString();
            Student.Degree deg=null;
            if (statusget.equals("MSc"))
            deg= Student.Degree.MSc;
            if (statusget.equals("PhD"))
                deg= Student.Degree.PhD;
            Student student = new Student(name,depertment,deg);
            JsonArray jsonArrayOfModels = studentJsonObject.get("models").getAsJsonArray();
            LinkedList<Model> modelList= new LinkedList<Model>();
            for (JsonElement mdl : jsonArrayOfModels){
                JsonObject modelJsonObject = mdl.getAsJsonObject();
                String modelName = modelJsonObject.get("name").getAsString();
                String typeget = modelJsonObject.get("type").getAsString();
                Data.Type type=null;
                if (typeget.equals("images")||typeget.equals("Images"))
                    type = Data.Type.Images;
                if (typeget.equals("Tabular")||typeget.equals("tabular"))
                    type = Data.Type.Tabular;
                if (typeget.equals("Text")||typeget.equals("text"))
                    type = Data.Type.Text;
                int size = modelJsonObject.get("size").getAsInt();
                Data data = new Data(type,size);
                Model model = new Model(data,modelName,student);
                modelList.add(model);
            }
            StudentService studentService = new StudentService(student.getName() + " service",student,modelList);
            studentServiceList.add(studentService);
            System.out.println(student.getName() + " " + student.getDepartment() + " " + student.getStatus());
            System.out.println(studentService.getName() + " " + studentService.getStudent().getName()+ ":");
            for (Model m : modelList){
                System.out.print(m.getName() + " " + m.getData().getSize() + " " + m.getData().getType()+ ", ");
            }
            System.out.println();
            Thread studentservicet = new Thread(studentService);
            threadList.add(studentservicet);
            studentservicet.start();
        }
        for (Thread thread : threadList)
            thread.join();
        for (StudentService studentService : studentServiceList){
            System.out.println(studentService.getStudent().getName() + " read " + studentService.getStudent().getPapersRead() + " and published: " + studentService.getStudent().getPublications());
            for (Model model : studentService.getModels()){
                if (model.getCurrStatus()== Model.Status.Trained||model.getCurrStatus()== Model.Status.Tested) {
                    System.out.print(model.getName() + " " + model.getCurrStatus() + " ");
                    if (!model.getResult().equals("None")) {
                        System.out.print(model.getResult());
                    }
                    System.out.println();
                }
            }
        }

        for (CPU cpu : Cluster.getInstance().getCPUS()){
            System.out.println("CPU time: " + cpu.getTime());
            System.out.println("CPU numofTicks: " + cpu.getNumOfTicks());
        }
        for (GPU gpu : Cluster.getInstance().getGPUS()){
            System.out.println("GPU time: " + gpu.getCurrTime());
        }

        for (ConferenceService cfs : cfsList){
            System.out.println(cfs.getName() + " published:");
            for (Model model : cfs.getCfsList()){
                System.out.println(model.getName());
            }
        }
        System.out.println("Amount of GPUTimeUnits: " + Cluster.getInstance().getStatistics().getGPUTimeUnits());
        System.out.println("Amount of CPUTimeUnits: " + Cluster.getInstance().getStatistics().getCPUTimeUnits());
        System.out.println("Amount of Batches processed by CPU: " + Cluster.getInstance().getStatistics().getCPUProcessed());

        //FileReader
        File file = new File("out.json");
        FileWriter fw = new FileWriter("out.json");
        PrintWriter pw = new PrintWriter(fw);
        pw.println("{");
        pw.println("    \"students\": [");
        for (StudentService studentService : studentServiceList) {
            pw.println("        {");
            pw.println("            \"name\": \"" + studentService.getStudent().getName() + "\""  + ",");
            pw.println("            \"department\": \"" + studentService.getStudent().getDepartment() + "\""  + ",");
            pw.println("            \"status\": \"" + studentService.getStudent().getStatus() + "\""  + ",");
            pw.println("            \"publications\": " + studentService.getStudent().getPublications() + ",");
            pw.println("            \"papersRead\": " + studentService.getStudent().getPapersRead()  + ",");
            pw.print("            \"trainedModels\": " + "[");
            boolean hasModel=false;
            boolean first = true;
            for (Model model : studentService.getModels()) {
                if (model.getCurrStatus() == Model.Status.Trained || model.getCurrStatus() == Model.Status.Tested) {
                    if (!hasModel)
                        pw.println();
                    if (!first){
                       pw.println(",");
                    }
                    first=false;
                    hasModel=true;
                    pw.println("                {");
                    pw.println("                    \"name\": "  + "\"" + model.getName() + "\""  + ",");
                    pw.println("                    \"data\": {");
                    pw.println("                        \"type\": " + "\"" + model.getData().getType() + "\""  + ",");
                    pw.println("                        \"size\": " + model.getData().getSize());
                    pw.println("                    },");
                    pw.println("                    \"status\": " + "\"" + model.getCurrStatus() + "\""  + ",");
                    pw.println("                    \"results\": " + "\"" + model.getResult() + "\"");
                    pw.print("                }");
                }
            }
            if (hasModel) {
                pw.println();
                pw.println("            ]");
            }
            else
            pw.println("]");
            pw.println("        },");
        }
        pw.println("    ],");
        pw.println("    \"conferences\": [");
        pw.println("        {");
        for (ConferenceService cfs : cfsList){
            pw.println("            \"name\": \"" + cfs.getCfi().getName() + "\""  + ",");
            pw.println("            \"date\": " + cfs.getCfi().getDate()  + ",");
            pw.println("            \"publications\":\"" + " [");
            for (Model model : cfs.getCfsList()){
                pw.println("                {");
                pw.println("                    \"name\": \"" + model.getName());
                pw.println("                    \"data\": {");
                pw.println("                        \"type\": \"" + model.getData().getType() + "\""  + ",");
                pw.println("                        \"size\": " + model.getData().getSize());
                pw.println("                    },");
                pw.println("                    \"status\": \"" + model.getCurrStatus() + "\""  + ",");
                pw.println("                    \"status\": \"" + model.getResult() + "\""  + ",");
                pw.println("                }");
            }
            pw.println("            ]");
            pw.println("        },");
        }
        pw.println("    ],");
        pw.println("gpuTimeUsed: " + Cluster.getInstance().getStatistics().getGPUTimeUnits());
        pw.println("cpuTimeUsed: " + Cluster.getInstance().getStatistics().getCPUTimeUnits());
        pw.println("batchesProcessed: " + Cluster.getInstance().getStatistics().getCPUProcessed());
        pw.close();
    }
}
