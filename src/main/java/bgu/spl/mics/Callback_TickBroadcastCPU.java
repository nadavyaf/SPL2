package bgu.spl.mics;

import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.GPU;

public class Callback_TickBroadcastCPU implements Callback<TickBroadcast> {
        private CPU cpu;
        public Callback_TickBroadcastCPU(CPU cpu){
            this.cpu=cpu;
        }
        public void call(TickBroadcast tick) throws InterruptedException {
            cpu.updateTime();
        }
    }

