package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;

import java.util.HashMap;

/**
 * GPU service is responsible for handling the
 * {@link //TrainModelEvent} and {@link //TestModelEvent},
 * in addition to sending the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    final private GPU gpu;
    private Callback_TickBroadcastGPU tick;
    private Callback_TestModelEvent test;
    private Callback_TrainModelEvent train;

    public GPUService(String name, GPU gpu) {
        super(gpu + " " + "service");
        this.gpu = gpu;
        tick = new Callback_TickBroadcastGPU(gpu);
        test = new Callback_TestModelEvent(this);
        train = new Callback_TrainModelEvent(); // NEED TO IMPLEMENT THE CALLBACK.
    }
    @Override
    protected void initialize() {
       MessageBusImpl.getInstance().register(this);
       this.subscribeBroadcast(TickBroadcast.class,tick);
       this.subscribeEvent(TestModelEvent.class,test);
       this.subscribeEvent(TrainModelEvent.class,train);
    }
    public Boolean isEventSubscribed(Event e){
        return MessageBusImpl.getInstance().isMicroServiceEventRegistered(this,e);
    }
    public Boolean isBroadcastSubscribed(Broadcast b){
        return MessageBusImpl.getInstance().isMicroServiceBroadCastRegistered(this,b);
    }

    public GPU getGpu() {
        return gpu;
    }
}

/**
 * Assiph's Comment: in GPU and CPU, both of the Services should be used to send and bring messages. So in GPU case, the
 * GPUService should be responsible for bringing the data to the GPU, and putting it in the right Queues, and also after 2 scenarios:
 * 1. The GPU will break the data into DataBatches, the GPUService should send them in a good manner to the cluster (without clogging the Cpus)
 * 2. The GPU should take the processed databatches from the Cluster, and insert them into the LearnQueue. From there it should be
 * the GPU responsibility to process the data.
 * In both cases, the GPUService should call the GPU functions - DivideALL and Divide1000 in case 1, and in case 2 it should
 * call the GPULearn function.
 * Im still not sure if we should change the GPULearn function to get a Queue of Databatches and make the GPU learn them all,
 * or we should keep it as is, and everytime the GPUService gets a new processed Databatch, it will call the function.
 */

