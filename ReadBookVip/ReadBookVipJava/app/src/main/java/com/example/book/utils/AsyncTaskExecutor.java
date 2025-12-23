package com.example.book.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AsyncTaskExecutor<Params, Result> {

    protected void onPreExecute(){}
    protected abstract Result doInBackground(Params... params);
    protected void onPostExecute(Result result){}
    protected void onCancelled() {}
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mInterruptIfRunning = false;

    @SafeVarargs
    public final synchronized void execute(final Params... params) {
        postPreExecute();

        mExecutorService.execute(() -> {
            try {
                checkInterrupted();
                final Result result = doInBackground(params);

                checkInterrupted();
                postPostExecute(result);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                postCancelled();
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mExecutorService.shutdown();
            }
        });
    }

    private void postPreExecute() {
        mHandler.post(this::onPreExecute);
    }

    private void postPostExecute(final Result result) {
        mHandler.post(() -> onPostExecute(result));
    }

    private void postCancelled() {
        mHandler.post(this::onCancelled);
    }

    private void checkInterrupted() throws InterruptedException {
        if (isInterrupted()){
            throw new InterruptedException();
        }
    }

    public void cancel(boolean interrupt){
        setInterrupted(interrupt);
    }

    public final boolean isCancelled() {
        return isInterrupted();
    }

    private boolean isInterrupted() {
        return mInterruptIfRunning;
    }

    private void setInterrupted(boolean isInterrupted) {
        mInterruptIfRunning = isInterrupted;
    }
}
