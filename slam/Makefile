COMPILE_SCRIPT = ./compileit.sh
PACKAGE_PREFIX = com/slam

### Targets ################################################################

all: utils interfaces impls

utils: \
	$(PACKAGE_PREFIX)/Landmark.class \
	$(PACKAGE_PREFIX)/Line.class \
	$(PACKAGE_PREFIX)/Pose.class \
	$(PACKAGE_PREFIX)/Ransac.class \
	$(PACKAGE_PREFIX)/Vector2D.class \
	$(PACKAGE_PREFIX)/Util.class

interfaces: \
	$(PACKAGE_PREFIX)/Arch.class \
	$(PACKAGE_PREFIX)/EKFServer.class \
	$(PACKAGE_PREFIX)/LandmarkServer.class \
	$(PACKAGE_PREFIX)/BeaconLandmarkServer.class \
	$(PACKAGE_PREFIX)/RansacLandmarkServer.class


impls: \
	$(PACKAGE_PREFIX)/ArchImpl.class \
	$(PACKAGE_PREFIX)/EKFServerImpl.class \
	$(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.class \
	$(PACKAGE_PREFIX)/RansacLandmarkServerImpl.class

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


### Interfaces ##############################################################

$(PACKAGE_PREFIX)/Arch.class: $(PACKAGE_PREFIX)/Arch.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Arch.java

$(PACKAGE_PREFIX)/EKFServer.class: $(PACKAGE_PREFIX)/EKFServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServer.java

$(PACKAGE_PREFIX)/LandmarkServer.class: $(PACKAGE_PREFIX)/LandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/LandmarkServefr.java

$(PACKAGE_PREFIX)/BeaconLandmarkServer.class: $(PACKAGE_PREFIX)/BeaconLandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/BeaconLandmarkServer.java

$(PACKAGE_PREFIX)/RansacLandmarkServer.class: $(PACKAGE_PREFIX)/RansacLandmarkServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RansacLandmarkServer.java


### Impls ###################################################################

$(PACKAGE_PREFIX)/ArchImpl.class: $(PACKAGE_PREFIX)/ArchImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/ArchImpl.java

$(PACKAGE_PREFIX)/EKFServerImpl.class: $(PACKAGE_PREFIX)/EKFServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServerImpl.java

$(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.class: $(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/BeaconLandmarkServerImpl.java

$(PACKAGE_PREFIX)/RansacLandmarkServerImpl.class: $(PACKAGE_PREFIX)/RansacLandmarkServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/RansacLandmarkServerImpl.java

