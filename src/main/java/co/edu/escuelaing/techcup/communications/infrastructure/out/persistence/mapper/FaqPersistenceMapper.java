package co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.mapper;

import co.edu.escuelaing.techcup.communications.domain.model.Faq;
import co.edu.escuelaing.techcup.communications.infrastructure.out.persistence.jpa.FaqDao;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;

/** Maps {@link FaqDao} to/from {@link Faq}. No associations, straightforward field mapping. */
@Mapper(componentModel = "spring")
public interface FaqPersistenceMapper {

    default Faq toDomain(FaqDao dao) {
        return Faq.fromPersistence(dao.getId(), dao.getKeywords(), dao.getAnswer(), dao.getCreatedAt(), dao.getUpdatedAt());
    }

    default FaqDao toDao(Faq faq) {
        FaqDao dao = new FaqDao();
        dao.setId(faq.getId());
        dao.setKeywords(new LinkedHashSet<>(faq.getKeywords()));
        dao.setAnswer(faq.getAnswer());
        dao.setCreatedAt(faq.getCreatedAt());
        dao.setUpdatedAt(faq.getUpdatedAt());
        return dao;
    }
}
