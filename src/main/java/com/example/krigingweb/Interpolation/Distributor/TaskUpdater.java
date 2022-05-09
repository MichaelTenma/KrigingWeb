package com.example.krigingweb.Interpolation.Distributor;

import com.example.krigingweb.Entity.LandEntity;
import com.example.krigingweb.Service.LandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
class TaskUpdater {
    private final LandService landService;
    private final ExecutorService updateExecutorService;

//    private final ThreadLocal<ByteBuffer> byteBufferThreadLocal = ThreadLocal.withInitial(() -> ByteBuffer.allocate(4096));
//    private final ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
    private FileOutputStream fileOutputStream;
    private BufferedOutputStream bufferedOutputStream;

    private final String filePath;

    public TaskUpdater(int maxUpdaterNumber, LandService landService, String filePath) {
        this.landService = landService;
        this.updateExecutorService = Executors.newFixedThreadPool(
            maxUpdaterNumber,
            new CustomizableThreadFactory("distributor-taskUpdater-")
        );
        this.filePath = filePath;
    }

    public void start(){
        try {
            fileOutputStream = new FileOutputStream(filePath);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 4096);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.bufferedOutputStream.close();
            this.fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(List<LandEntity> landEntityList) throws IOException {
        byte[] bytes = new byte[7 * 8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        for(LandEntity landEntity : landEntityList){
            /* land_id UUID */
            UUID land_id = landEntity.getLandId();
            byteBuffer.putLong(land_id.getMostSignificantBits());
            byteBuffer.putLong(land_id.getLeastSignificantBits());
            byteBuffer.putDouble(landEntity.getN());
            byteBuffer.putDouble(landEntity.getP());
            byteBuffer.putDouble(landEntity.getK());
            byteBuffer.putDouble(landEntity.getOC());
            byteBuffer.putDouble(landEntity.getPH());

            bufferedOutputStream.write(byteBuffer.array());
            byteBuffer.clear();
        }
    }

    public CompletableFuture<Void> update(List<LandEntity> landEntityList){
//        return CompletableFuture.runAsync(() -> {
            try {
                this.write(landEntityList);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
//        }, updateExecutorService);

        return null;
//        return CompletableFuture.runAsync(() -> {
//            int i = 0;
//            for(; i < 5;i++){
//                try {
//                    this.landService.updateLand(landEntityList);
//                    break;/* 正常情况执行一次即退出 */
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                    Thread.yield();/* 给数据库死锁恢复一些时间 */
//                }
//            }
//            if(i == 5){
//                throw new RuntimeException();
//            }
//        }, this.updateExecutorService);
    }
}