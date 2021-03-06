COMPILE_SCRIPT = ./compileit.sh
PACKAGE_PREFIX = com/slam

### Targets ################################################################

all: utils interfaces impls visuals tests

utils: \
	$(PACKAGE_PREFIX)/Landmark.class \
	$(PACKAGE_PREFIX)/Line.class \
	$(PACKAGE_PREFIX)/Pose.class \
	$(PACKAGE_PREFIX)/Ransac.class \
	$(PACKAGE_PREFIX)/Vector2D.class \
	$(PACKAGE_PREFIX)/Util.class \
	$(PACKAGE_PREFIX)/RobotInfo.class

interfaces: \
	$(PACKAGE_PREFIX)/Arch.class \
	$(PACKAGE_PREFIX)/EKFServer.class \
	$(PACKAGE_PREFIX)/LandmarkServer.class \
	$(PACKAGE_PREFIX)/BeaconLandmarkServer.class \
	$(PACKAGE_PREFIX)/RansacLandmarkServer.class \
	$(PACKAGE_PREFIX)/MappingServer.class \
	$(PACKAGE_PREFIX)/RotCenter.class

impls: \
	$(PACKAGE_PREFIX)/ArchImpl.class \
	$(PACKAGE_PREFIX)/EKFServerImpl.class \
	$(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.class \
	$(PACKAGE_PREFIX)/RansacLandmarkServerImpl.class \
	$(PACKAGE_PREFIX)/MappingServerImpl.class \
	$(PACKAGE_PREFIX)/RotCenterImpl.class

visuals: \
	$(PACKAGE_PREFIX)/MappingServerVisData.class \
	$(PACKAGE_PREFIX)/MappingServerVis.class

tests: \
	$(PACKAGE_PREFIX)/TestVector2D.class \
	$(PACKAGE_PREFIX)/TestRansac.class \
	$(PACKAGE_PREFIX)/TestUtil.class


### Utils ###################################################################

$(PACKAGE_PREFIX)/Landmark.class: $(PACKAGE_PREFIX)/Landmark.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Landmark.java

$(PACKAGE_PREFIX)/Line.class: $(PACKAGE_PREFIX)/Line.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Line.java

$(PACKAGE_PREFIX)/Pose.class: $(PACKAGE_PREFIX)/Pose.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Pose.java

$(PACKAGE_PREFIX)/Ransac.class: $(PACKAGE_PREFIX)/Ransac.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Ransac.java

$(PACKAGE_PREFIX)/Vector2D.class: $(PACKAGE_PREFIX)/Vector2D.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Vector2D.java

$(PACKAGE_PREFIX)/Util.class: $(PACKAGE_PREFIX)/Util.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Util.java

$(PACKAGE_PREFIX)/RobotInfo.class: $(PACKAGE_PREFIX)/RobotInfo.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RobotInfo.java


### Interfaces ##############################################################

$(PACKAGE_PREFIX)/Arch.class: $(PACKAGE_PREFIX)/Arch.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Arch.java

$(PACKAGE_PREFIX)/EKFServer.class: $(PACKAGE_PREFIX)/EKFServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServer.java

$(PACKAGE_PREFIX)/LandmarkServer.class: $(PACKAGE_PREFIX)/LandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/LandmarkServer.java

$(PACKAGE_PREFIX)/BeaconLandmarkServer.class: $(PACKAGE_PREFIX)/BeaconLandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/BeaconLandmarkServer.java

$(PACKAGE_PREFIX)/RansacLandmarkServer.class: $(PACKAGE_PREFIX)/RansacLandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RansacLandmarkServer.java

$(PACKAGE_PREFIX)/MappingServer.class: $(PACKAGE_PREFIX)/MappingServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/MappingServer.java

$(PACKAGE_PREFIX)/RotCenter.class: $(PACKAGE_PREFIX)/RotCenter.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RotCenter.java


### Impls ###################################################################

$(PACKAGE_PREFIX)/ArchImpl.class: $(PACKAGE_PREFIX)/ArchImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/ArchImpl.java

$(PACKAGE_PREFIX)/EKFServerImpl.class: $(PACKAGE_PREFIX)/EKFServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServerImpl.java

$(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.class: $(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.java

$(PACKAGE_PREFIX)/RansacLandmarkServerImpl.class: $(PACKAGE_PREFIX)/RansacLandmarkServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RansacLandmarkServerImpl.java

$(PACKAGE_PREFIX)/MappingServerImpl.class: $(PACKAGE_PREFIX)/MappingServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/MappingServerImpl.java

$(PACKAGE_PREFIX)/RotCenterImpl.class: $(PACKAGE_PREFIX)/RotCenterImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RotCenterImpl.java


### Visuals #################################################################

$(PACKAGE_PREFIX)/MappingServerVisData.class: $(PACKAGE_PREFIX)/MappingServerVisData.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/MappingServerVisData.java

$(PACKAGE_PREFIX)/MappingServerVis.class: $(PACKAGE_PREFIX)/MappingServerVis.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/MappingServerVis.java


### Tests ###################################################################

$(PACKAGE_PREFIX)/TestVector2D.class: $(PACKAGE_PREFIX)/TestVector2D.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/TestVector2D.java

$(PACKAGE_PREFIX)/TestRansac.class: $(PACKAGE_PREFIX)/TestRansac.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/TestRansac.java

$(PACKAGE_PREFIX)/TestUtil.class: $(PACKAGE_PREFIX)/TestUtil.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/TestUtil.java

