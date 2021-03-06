package ar.edu.itba.pedestriansim.metric;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ar.edu.itba.pedestriansim.back.entity.PedestrianAreaFileSerializer.DymaimcFileStep;
import ar.edu.itba.pedestriansim.back.entity.PedestrianAreaFileSerializer.PedestrianDynamicLineInfo;
import ar.edu.itba.pedestriansim.back.entity.PedestrianAreaFileSerializer.StaticFileLine;
import ar.edu.itba.pedestriansim.metric.component.AverageTravelTime;
import ar.edu.itba.pedestriansim.metric.component.AverageTurnedAngle;
import ar.edu.itba.pedestriansim.metric.component.AverageVelocity;
import ar.edu.itba.pedestriansim.metric.component.AverageWalkDistance;
import ar.edu.itba.pedestriansim.metric.component.CollitionCount;
import ar.edu.itba.pedestriansim.metric.component.CollitionCountPerInstant;
import ar.edu.itba.pedestriansim.metric.component.CollitionMetric;
import ar.edu.itba.pedestriansim.metric.component.Metric;
import ar.edu.itba.pedestriansim.metric.component.SimpleMetric;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CalculateMetricsFromFile {

	private Map<Integer, StaticFileLine> _staticPedestrianInfoById = Maps.newHashMap();
	private Supplier<DymaimcFileStep> _stepsSupplier;
	private List<CollitionMetric> collitionMetrics;
	private List<SimpleMetric> simpleMetrics;
	private List<Metric> allMetrics;
	private FileWriter _outputFileWriter;

	private float _lastDt = 0;

	public CalculateMetricsFromFile(Supplier<StaticFileLine> staticStepSupplier, Supplier<DymaimcFileStep> stepsSupplier, FileWriter outputFileWriter) {
		_stepsSupplier = stepsSupplier;
		boolean staticSupplierFinished;
		do {
			StaticFileLine fileLine = staticStepSupplier.get();
			staticSupplierFinished = fileLine == null;
			if (!staticSupplierFinished) {
				_staticPedestrianInfoById.put(fileLine.id(), fileLine);
			}
		} while (!staticSupplierFinished);
		_outputFileWriter = outputFileWriter;
		collitionMetrics = Lists.newArrayList();
		simpleMetrics = Lists.newArrayList();
		allMetrics = Lists.newArrayList();
		addCollitionMetric(new CollitionCount());
		addCollitionMetric(new CollitionCountPerInstant());
		addSimpleMetric(new AverageVelocity());
		addSimpleMetric(new AverageTravelTime());
		addSimpleMetric(new AverageWalkDistance());
		// addSimpleMetric(new VelocityByDensity());
		addSimpleMetric(new AverageTurnedAngle());
	}

	public CalculateMetricsFromFile appendHeaderIf(boolean condition) throws IOException {
		if (condition) { 
			for (Metric metric : allMetrics) {
				_outputFileWriter.append(metric.name() + "\t");
			}
			_outputFileWriter.append("\n");
		}
		return this;
	}

	private void addCollitionMetric(CollitionMetric metric) {
		collitionMetrics.add(metric);
		allMetrics.add(metric);
	}

	private void addSimpleMetric(SimpleMetric metric) {
		simpleMetrics.add(metric);
		allMetrics.add(metric);
	}

	public void runMetrics() {
		_lastDt = 0;
		while (update()) {
			// No op.
		}
		onSimulationEnd();
	}

	public boolean update() {
		for (Metric metric : allMetrics) {
			metric.onIterationStart();
		}
		DymaimcFileStep step = _stepsSupplier.get();
		if (step == null) {
			return false; // XXX: simulation finished!
		}
		float dt = step.step() - _lastDt;
		_lastDt = step.step();
		List<PedestrianDynamicLineInfo> lines = step.pedestriansInfo();
		for (int i = 0; i < lines.size(); i++) {
			PedestrianDynamicLineInfo linei = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PedestrianDynamicLineInfo linej = lines.get(j);
				StaticFileLine staticLinei = _staticPedestrianInfoById.get(linei.id());
				StaticFileLine staticLinej = _staticPedestrianInfoById.get(linej.id());
				float centerDist = linei.center().distance(linej.center());
				float deltaDist = 0.08f;
				if (centerDist <= staticLinei.radius() + staticLinej.radius() + deltaDist) {
					for (CollitionMetric metric : collitionMetrics) {
						metric.onCollition(dt, linei.id(), linej.id());
					}
				}
			}
			for (SimpleMetric metric : simpleMetrics) {
				metric.update(dt, linei, _staticPedestrianInfoById.get(linei.id()));
			}
		}
		for (Metric metric : allMetrics) {
			metric.onIterationEnd();
		}
		return true;
	}

	public void onSimulationEnd() {
		try {
			for (Metric metric : allMetrics) {
				metric.appendResults(_outputFileWriter);
				_outputFileWriter.append("\t");
			}
			_outputFileWriter.append("\n");
			_outputFileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
