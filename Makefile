SCRIPTS_PATH      := scripts

.PHONY: tag
tag: 
	$(SCRIPTS_PATH)/tag.sh ${BUILD_BRANCH} ${BUILD_NUM}

.PHONY: is-prerelease
is-prerelease: 
	@ $(SCRIPTS_PATH)/prerelease.sh ${BUILD_BRANCH}

.PHONY: release-notes
release-notes: 
	@ $(SCRIPTS_PATH)/release-notes.sh

.PHONY: multichain-artifacts
multichain-artifacts: 
	$(SCRIPTS_PATH)/multichain-artifacts.sh ${BUILD_TAG}

.PHONY: hyperledger-fabric-artifacts
hyperledger-fabric-artifacts: 
	$(SCRIPTS_PATH)/hyperledger-fabric-artifacts.sh ${BUILD_TAG}

.PHONY: corda-artifacts
corda-artifacts: 
	$(SCRIPTS_PATH)/corda-artifacts.sh ${BUILD_TAG}