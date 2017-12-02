package com.aurora.solar.common.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
	public static final String UTF_8_ENCODING = "UTF-8";
	
	public StreamUtils() {	}
	public static void close(InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void close(OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}			
		}
	}

	public static String streamToString(InputStream inputStream) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			copy(inputStream, outputStream);
			return outputStream.toString(UTF_8_ENCODING);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static void copy(InputStream inputStream, OutputStream ... outputStreams) 
			throws RuntimeException, InterruptedException {
		copy(true, true, inputStream, outputStreams);
	}
	
	public static void copy(boolean closeInputStream, boolean closeOutputStreams, InputStream inputStream, 
			OutputStream ... outputStreams) throws RuntimeException, InterruptedException {
		copy(0, closeInputStream, closeOutputStreams, inputStream, outputStreams);
	}

	public static long copy(int bytesPerSecond, boolean closeInputStream, boolean closeOutputStreams,
			InputStream inputStream, OutputStream ... outputStreams) throws RuntimeException, InterruptedException {
		return copy(bytesPerSecond, 1000, closeInputStream, closeOutputStreams, inputStream, outputStreams);
	}

	public static long copy(int bytesPerInterval, int intervalInMillis, boolean closeInputStream, 
			boolean closeOutputStreams, InputStream inputStream, OutputStream ... outputStreams) 
				throws RuntimeException, InterruptedException {
		
		StringBuilder errors = new StringBuilder();
		
		if (inputStream == null) {
			errors.append("No input stream handle provided! ");
		}
		
		if (outputStreams == null) {
			errors.append("No output stream handle(s) provided! ");
		}
				
		if (errors.length() != 0) {
			throw new IllegalArgumentException(
				"Unable to copy bytes from input to output stream(s): " + 
					errors.toString().trim());
		}
		
		int nextByte = 0;
		int numBytesCopied = 0;			
		long totalBytesCopied = 0;
		
		try {									
			long startTime = System.currentTimeMillis();
			
			while ((nextByte = inputStream.read()) > -1) {
				for (OutputStream outputStream : outputStreams) {
					outputStream.write(nextByte);
				}
		
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
				}
				
				numBytesCopied++;
				totalBytesCopied++;
								
				if (numBytesCopied == bytesPerInterval) {
					numBytesCopied = 0;
					
					long sleepTime = intervalInMillis - (System.currentTimeMillis() - startTime);
					
					if (sleepTime > 0) {
						Thread.sleep(sleepTime);
					}
					
					startTime = System.currentTimeMillis();
				}
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
				"Error copying bytes from input to output stream(s): " + e.getMessage(), e);
		} finally {
			if (closeInputStream) {
				close(inputStream);
			}
				
			for (OutputStream outputStream : outputStreams) {
				if (closeOutputStreams) {
					close(outputStream);
				} else {
					try { outputStream.flush(); } catch (Exception e) { }
				}
			}
		}

		return totalBytesCopied;
	}	
}
