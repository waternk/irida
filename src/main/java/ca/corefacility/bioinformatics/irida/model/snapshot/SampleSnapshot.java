package ca.corefacility.bioinformatics.irida.model.snapshot;

import java.util.Date;

import javax.persistence.Lob;

import ca.corefacility.bioinformatics.irida.model.sample.IridaSample;

public class SampleSnapshot implements IridaSample {

	private Long id;

	private String sequencerSampleId;

	private String sampleName;

	@Lob
	private String description;

	private String organism;

	private String isolate;

	private String strain;

	private String collectedBy;

	private Date collectionDate;

	private String geographicLocationName;

	@Lob
	private String isolationSource;

	private String latitude;

	private String longitude;

	private String cultureCollection;

	@Lob
	private String genotype;

	@Lob
	private String passageHistory;

	private String pathotype;

	private String serotype;

	private String serovar;

	private String specimenVoucher;

	private String subgroup;

	private String subtype;

	public SampleSnapshot(IridaSample sample) {
		this.id = sample.getId();
		this.sequencerSampleId = sample.getSequencerSampleId();
		this.sampleName = sample.getSampleName();
		this.description = sample.getDescription();
		this.organism = sample.getOrganism();
		this.isolate = sample.getIsolate();
		this.strain = sample.getStrain();
		this.collectedBy = sample.getCollectedBy();
		this.collectionDate = sample.getCollectionDate();
		this.geographicLocationName = sample.getGeographicLocationName();
		this.isolationSource = sample.getIsolationSource();
		this.latitude = sample.getLatitude();
		this.longitude = sample.getLongitude();
		this.cultureCollection = sample.getCultureCollection();
		this.genotype = sample.getGenotype();
		this.passageHistory = sample.getPassageHistory();
		this.pathotype = sample.getPathotype();
		this.serotype = sample.getSerotype();
		this.serovar = sample.getSerovar();
		this.specimenVoucher = sample.getSpecimenVoucher();
		this.subgroup = sample.getSubgroup();
		this.subtype = sample.getSubtype();
	}

	public Long getId() {
		return id;
	}

	public String getSequencerSampleId() {
		return sequencerSampleId;
	}

	public String getSampleName() {
		return sampleName;
	}

	public String getDescription() {
		return description;
	}

	public String getOrganism() {
		return organism;
	}

	public String getIsolate() {
		return isolate;
	}

	public String getStrain() {
		return strain;
	}

	public String getCollectedBy() {
		return collectedBy;
	}

	public Date getCollectionDate() {
		return collectionDate;
	}

	public String getGeographicLocationName() {
		return geographicLocationName;
	}

	public String getIsolationSource() {
		return isolationSource;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getCultureCollection() {
		return cultureCollection;
	}

	public String getGenotype() {
		return genotype;
	}

	public String getPassageHistory() {
		return passageHistory;
	}

	public String getPathotype() {
		return pathotype;
	}

	public String getSerotype() {
		return serotype;
	}

	public String getSerovar() {
		return serovar;
	}

	public String getSpecimenVoucher() {
		return specimenVoucher;
	}

	public String getSubgroup() {
		return subgroup;
	}

	public String getSubtype() {
		return subtype;
	}

}
