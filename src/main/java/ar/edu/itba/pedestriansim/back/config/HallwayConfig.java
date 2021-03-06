package ar.edu.itba.pedestriansim.back.config;

import org.apache.commons.lang3.tuple.Pair;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Vector2f;

import com.google.common.collect.Range;

import ar.edu.itba.common.rand.UniformRandomGenerator;
import ar.edu.itba.pedestriansim.back.entity.PedestrianAppConfig;
import ar.edu.itba.pedestriansim.back.entity.PedestrianArea;
import ar.edu.itba.pedestriansim.back.entity.PedestrianSource;
import ar.edu.itba.pedestriansim.back.entity.Wall;
import ar.edu.itba.pedestriansim.back.entity.mision.PedestrianMision;
import ar.edu.itba.pedestriansim.back.entity.mision.PedestrianTargetArea;

// 17 mts recorridos en promedio
public class HallwayConfig implements ApplicationConfigBuilder {

	private final float delay = 1f;
	
	private ApplicationConfigBuilder _defaultBuilder;
	
	public HallwayConfig() {
		this(new DefaultPedestrianAppConfig());
	}

	public HallwayConfig(ApplicationConfigBuilder defaultBuilder) {
		_defaultBuilder = defaultBuilder;
	}

	@Override
	public PedestrianAppConfig get() {
		PedestrianAppConfig config = _defaultBuilder.get();
		config.pedestrianFactory().setPedestrianAlphaBeta(Pair.of(1000f, Range.closed(0.4f, 0.5f)));
		config.pedestrianFactory().setFuturePedestrianAlphaBeta(1000f, 0.2f);
		addWalls(config.pedestrianArea());
		addSource1(config.pedestrianArea());
		addSource2(config.pedestrianArea());
		return config;
	}

	private void addSource1(PedestrianArea area) {
		// derecha
		PedestrianMision mission = new PedestrianMision();
		area.addSource(
			new PedestrianSource(new Vector2f(27, 17), 1f, mission, 1)
				.setProduceDelayGenerator(new UniformRandomGenerator(delay, delay))
				.setPedestrianAmountGenerator(new UniformRandomGenerator(1, 1))
		);
		mission.putFirst(new PedestrianTargetArea(new Line(10, 15, 10, 19)));
	}

	private void addSource2(PedestrianArea area) {
		// izquierda
		PedestrianMision mission = new PedestrianMision();
		area.addSource(
			new PedestrianSource(new Vector2f(5, 17), 1f, mission, 2)
				.setProduceDelayGenerator(new UniformRandomGenerator(delay, delay))
				.setPedestrianAmountGenerator(new UniformRandomGenerator(1, 1))
		);
		mission.putFirst(new PedestrianTargetArea(new Line(22, 15, 22, 19)));
	}
	
	private void addWalls(PedestrianArea area) {
		// Arriba
		area.addObstacle(
			new Wall(new Line(0, 15, 30, 15)).setThickDirection(new Vector2f(0, 1))
		);
		// Abajo
		area.addObstacle(
			new Wall(new Line(0, 19, 30, 19)).setThickDirection(new Vector2f(0, -1))
		);
	}
}
