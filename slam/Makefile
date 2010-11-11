COMPILE_SCRIPT = ./compileit.sh
PACKAGE_PREFIX = com/slam

### Targets ################################################################

all: utils interfaces impls

utils: \
	$(PACKAGE_PREFIX)/Pose.class \
	$(PACKAGE_PREFIX)/Vector2D.class

interfaces: \
	$(PACKAGE_PREFIX)/Arch.class \
	$(PACKAGE_PREFIX)/EKFServer.class

impls: \
	$(PACKAGE_PREFIX)/ArchImpl.class \
	$(PACKAGE_PREFIX)/EKFServerImpl.class

### Utils ###################################################################

$(PACKAGE_PREFIX)/Pose.class: $(PACKAGE_PREFIX)/Pose.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Pose.java

$(PACKAGE_PREFIX)/Vector2D.class: $(PACKAGE_PREFIX)/Vector2D.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Vector2D.java


### Interfaces ##############################################################

$(PACKAGE_PREFIX)/Arch.class: $(PACKAGE_PREFIX)/Arch.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/Arch.java

$(PACKAGE_PREFIX)/EKFServer.class: $(PACKAGE_PREFIX)/EKFServer.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServer.java


### Impls ###################################################################

$(PACKAGE_PREFIX)/ArchImpl.class: $(PACKAGE_PREFIX)/ArchImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/ArchImpl.java

$(PACKAGE_PREFIX)/EKFServerImpl.class: $(PACKAGE_PREFIX)/EKFServerImpl.java
	$(COMPILE_SCRIPT) $(PACKAGE_PREFIX)/EKFServerImpl.java

