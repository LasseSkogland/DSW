package skogland.lasse.DSW.lwjglwrapper;


import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

import java.nio.*;
import java.util.Vector;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class OpenCL {

	private class CLDevice {
		private long platformID;
		private long deviceID;
		private String deviceName;
		private boolean isGPU;
		private CLCapabilities deviceCapabilities;
	}

	public static class CLPointer implements Pointer {
		private long address;

		public CLPointer(long address) {
			super();
			this.address = address;
		}

		@Override
		public long address() {
			return address;
		}
	}

	public Vector<CLDevice> clDevices;
	private CLDevice bestDev;
	private IntBuffer err = BufferUtils.createIntBuffer(1);

	private static int KERNEL_ARG_INCREMENTOR = 0;

	public OpenCL() {
		clDevices = new Vector<>();

		try (MemoryStack stack = stackPush()) {
			IntBuffer pi = stack.mallocInt(1);
			clGetPlatformIDs(null, pi);
			PointerBuffer platforms = stack.mallocPointer(pi.get(0));
			clGetPlatformIDs(platforms, (IntBuffer) null);
			for (int i = 0; i < platforms.capacity(); i++) {
				long platform = platforms.get(0);
				CLCapabilities platCap = CL.createPlatformCapabilities(platform);
				clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, null, pi);
				PointerBuffer devices = stack.mallocPointer(pi.get(0));
				clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, devices, (IntBuffer) null);
				for (int d = 0; i < devices.capacity(); i++) {
					long deviceID = devices.get(0);
					CLDevice dev = new CLDevice();
					dev.platformID = platform;
					dev.deviceID = deviceID;
					dev.deviceName = InfoUtil.getDeviceInfoStringUTF8(deviceID, CL_DEVICE_NAME);
					dev.isGPU = InfoUtil.getDeviceInfoLong(deviceID, CL_DEVICE_TYPE) == CL_DEVICE_TYPE_GPU;
					dev.deviceCapabilities = CL.createDeviceCapabilities(deviceID, platCap);
					clDevices.add(dev);
				}
			}
		}
		for (CLDevice dev : clDevices) {
			if (dev.isGPU) bestDev = dev;
			System.out.printf("PlaID: %d, DevID: %d, DevName: %s, isGpu: %s\n", dev.platformID, dev.deviceID, dev.deviceName, dev.isGPU);

		}
	}

	public long createContext(long clDeviceID) {
		PointerBuffer props = BufferUtils.createPointerBuffer(3);
		props.put(CL_CONTEXT_PLATFORM).put(bestDev.platformID).put(0).flip();
		long context = clCreateContext(props, clDeviceID == 0 ? bestDev.deviceID : clDeviceID, null, 0, err);
		InfoUtil.checkCLError(err);
		return context;
	}

	public long createCommandQueue(long clContext, long clDeviceID) {
		long cmd = clCreateCommandQueue(clContext, clDeviceID == 0 ? bestDev.deviceID : clDeviceID, 0, err);
		InfoUtil.checkCLError(err);
		return cmd;
	}

	public long createProgram(long clContext, String clSource) {
		return createProgram(clContext, clSource, "");
	}

	public long createProgram(long clContext, String clSource, String buildOptions) {
		long clProgram = clCreateProgramWithSource(clContext, clSource, err);
		clBuildProgram(clProgram, bestDev.deviceID, buildOptions, null, 0);
		String buildLog = InfoUtil.getProgramBuildInfoStringASCII(clProgram, bestDev.deviceID, CL_PROGRAM_BUILD_LOG);
		if (!buildLog.equals("\n")) System.out.printf("Build Error!\nBuild log: %s\n", buildLog);
		InfoUtil.checkCLError(err);
		return clProgram;
	}

	public long createKernel(long clProgram, String clKernelName) {
		long kernel = clCreateKernel(clProgram, clKernelName, err);
		InfoUtil.checkCLError(err);
		return kernel;
	}

	public CLPointer createOutputBuffer(long clContext, int size) {
		CLPointer clP = new CLPointer(nclCreateBuffer(clContext, CL_MEM_WRITE_ONLY, size, 0, 0));
		InfoUtil.checkCLError(err);
		return clP;
	}

	public CLPointer createInputBuffer(long clContext, int size) {
		return new CLPointer(nclCreateBuffer(clContext, CL_MEM_READ_ONLY, size, 0, 0));
	}

	public CLPointer createIOBuffer(long clContext, int size) {
		return new CLPointer(nclCreateBuffer(clContext, CL_MEM_READ_WRITE, size, 0, 0));
	}


	public void setKernelArg(long clKernel, Object arg) {
		int err = 0;
		if (arg instanceof ByteBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (ByteBuffer) arg);
		else if (arg instanceof DoubleBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (DoubleBuffer) arg);
		else if (arg instanceof FloatBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (FloatBuffer) arg);
		else if (arg instanceof IntBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (IntBuffer) arg);
		else if (arg instanceof LongBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (LongBuffer) arg);
		else if (arg instanceof PointerBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (PointerBuffer) arg);
		else if (arg instanceof ShortBuffer) err = clSetKernelArg(clKernel, KERNEL_ARG_INCREMENTOR++, (ShortBuffer) arg);
		else if (arg instanceof Byte) err = clSetKernelArg1b(clKernel, KERNEL_ARG_INCREMENTOR++, (Byte) arg);
		else if (arg instanceof Double) err = clSetKernelArg1d(clKernel, KERNEL_ARG_INCREMENTOR++, (Double) arg);
		else if (arg instanceof Float) err = clSetKernelArg1f(clKernel, KERNEL_ARG_INCREMENTOR++, (Float) arg);
		else if (arg instanceof Integer) err = clSetKernelArg1i(clKernel, KERNEL_ARG_INCREMENTOR++, (Integer) arg);
		else if (arg instanceof Long) err = clSetKernelArg1l(clKernel, KERNEL_ARG_INCREMENTOR++, (Long) arg);
		else if (arg instanceof CLPointer)
			err = clSetKernelArg1p(clKernel, KERNEL_ARG_INCREMENTOR++, ((CLPointer) arg).address);
		else if (arg instanceof Short) err = clSetKernelArg1s(clKernel, KERNEL_ARG_INCREMENTOR++, (Short) arg);
		InfoUtil.checkCLError(err);
	}

	public void executeKernel(long clKernel, long clCommandQueue, int clWorkSize) {
		KERNEL_ARG_INCREMENTOR = 0;
		PointerBuffer local = BufferUtils.createPointerBuffer(1);
		PointerBuffer global = BufferUtils.createPointerBuffer(1).put(0, clWorkSize);
		clGetKernelWorkGroupInfo(clKernel, bestDev.deviceID, CL_KERNEL_WORK_GROUP_SIZE, local, null);
		clEnqueueNDRangeKernel(clCommandQueue, clKernel, 1, null, global, local, null, null);
		clFinish(clCommandQueue);
	}

	public void executeKernel(long clKernel, long clCommandQueue, int clWorkSize1, int clWorkSize2) {
		KERNEL_ARG_INCREMENTOR = 0;
		PointerBuffer local = BufferUtils.createPointerBuffer(2);
		PointerBuffer global = BufferUtils.createPointerBuffer(2).put(0, clWorkSize1).put(1, clWorkSize2);
		clGetKernelWorkGroupInfo(clKernel, bestDev.deviceID, CL_KERNEL_WORK_GROUP_SIZE, local, null);
		long tmp = (long)Math.sqrt(local.get(0));
		local.put(0, tmp);
		local.put(1, tmp);
		clEnqueueNDRangeKernel(clCommandQueue, clKernel, 2, null, global, local, null, null);
		clFinish(clCommandQueue);
	}

	public void releaseBuffer(CLPointer clBuffer) {
		clReleaseMemObject(clBuffer.address);
	}

	public void releaseKernel(long clKernel) {

		clReleaseKernel(clKernel);
	}

	public void releaseProgram(long clProgram) {
		clReleaseProgram(clProgram);
	}

	public void releaseContext(long clContext, long clCommandQueue) {
		clReleaseCommandQueue(clCommandQueue);
		clReleaseContext(clContext);
	}

	public void readBuffer(long clCommandQueue, CLPointer buffer, Buffer result) {
		if (result instanceof ByteBuffer)
			clEnqueueReadBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (ByteBuffer) result, null, null);
		else if (result instanceof DoubleBuffer)
			clEnqueueReadBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (DoubleBuffer) result, null, null);
		else if (result instanceof FloatBuffer)
			clEnqueueReadBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (FloatBuffer) result, null, null);
		else if (result instanceof IntBuffer)
			clEnqueueReadBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (IntBuffer) result, null, null);
		else if (result instanceof ShortBuffer)
			clEnqueueReadBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (ShortBuffer) result, null, null);
	}

	public void writebuffer(long clCommandQueue, CLPointer buffer, Buffer data) {
		if (data instanceof ByteBuffer)
			clEnqueueWriteBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (ByteBuffer) data, null, null);
		else if (data instanceof DoubleBuffer)
			clEnqueueWriteBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (DoubleBuffer) data, null, null);
		else if (data instanceof FloatBuffer)
			clEnqueueWriteBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (FloatBuffer) data, null, null);
		else if (data instanceof IntBuffer)
			clEnqueueWriteBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (IntBuffer) data, null, null);
		else if (data instanceof ShortBuffer)
			clEnqueueWriteBuffer(clCommandQueue, buffer.address, CL_TRUE, 0, (ShortBuffer) data, null, null);
	}
}
